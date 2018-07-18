/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.control.VelocityPIDFController
import frc.team5190.lib.geometry.Pose2dWithCurvature
import frc.team5190.lib.geometry.Translation2d
import frc.team5190.lib.trajectory.TrajectoryFollower
import frc.team5190.lib.trajectory.TrajectoryIterator
import frc.team5190.lib.trajectory.TrajectorySamplePoint
import frc.team5190.lib.trajectory.TrajectoryUtil
import frc.team5190.lib.trajectory.timing.TimedState
import frc.team5190.robot.Constants
import frc.team5190.robot.Kinematics
import frc.team5190.robot.Localization
import frc.team5190.robot.auto.Trajectories

class FollowTrajectoryCommand(identifier: String, pathMirrored: Boolean = false) : Command() {

    // Notifier objects
    private val pf = Object()
    private val notifier: Notifier
    private var stopNotifier = false

    // Trajectory
    private var trajectory = Trajectories[identifier]

    // Path follower
    private val trajectoryFollower: TrajectoryFollower

    // PIDF controllers
    private val lController: VelocityPIDFController
    private val rController: VelocityPIDFController

    init {
        requires(DriveSubsystem)

        if (pathMirrored) {
            trajectory = TrajectoryUtil.mirrorTimed(trajectory)
        }

        // Initialize path follower
        trajectoryFollower = TrajectoryFollower(trajectory = trajectory)

        lController = VelocityPIDFController(
                kP = Constants.kPLeftDriveVelocity,
                kI = Constants.kILeftDriveVelocity,
                kV = Constants.kVLeftDriveVelocity,
                kS = Constants.kSLeftDriveVelocity,
                current = { DriveSubsystem.leftVelocity.FPS }
        )

        rController = VelocityPIDFController(
                kP = Constants.kPRightDriveVelocity,
                kI = Constants.kIRightDriveVelocity,
                kV = Constants.kVRightDriveVelocity,
                kS = Constants.kSRightDriveVelocity,
                current = { DriveSubsystem.rightVelocity.FPS }
        )


        // Initialize notifier
        notifier = Notifier {
            synchronized(pf) {
                if (stopNotifier) {
                    return@Notifier
                }

                val output = Kinematics.inverseKinematics(
                        trajectoryFollower.getRobotVelocity(Localization.robotPosition)
                )

                DriveSubsystem.set(ControlMode.PercentOutput,
                        lController.getPIDFOutput(output.first to 0.0),
                        rController.getPIDFOutput(output.second to 0.0))

                updateDashboard()
                System.out.printf("[Trajectory Follower] X Error: %3.3f, Y Error: %3.3f, T Error: %3.3f",
                        trajectoryFollower.currentPointPose.translation.x - Localization.robotPosition.translation.x,
                        trajectoryFollower.currentPointPose.translation.y - Localization.robotPosition.translation.y,
                        trajectoryFollower.currentPointPose.rotation.degrees - Localization.robotPosition.rotation.degrees)
            }
        }
    }

    fun addMarkerAt(waypoint: Translation2d): Marker {
        // Iterate through the trajectory and add a data point every 50 ms.
        val iterator = TrajectoryIterator(trajectory.indexView)
        val dataArray = arrayListOf<TrajectorySamplePoint<TimedState<Pose2dWithCurvature>>>()

        while (!iterator.isDone) {
            dataArray.add(iterator.advance(0.05))
        }

        // Find t where the distance between the provided waypoint and the actual point is shortest.
        val t = dataArray.minBy { waypoint.distance(it.state.state.translation) }!!.state?.t
        return Marker(this, t)
    }

    fun hasCrossedMarker(marker: Marker): Boolean {
        return marker.instance == this && trajectoryFollower.currentPoint.state.t > marker.t
    }

    private fun updateDashboard() {
        pathX = trajectoryFollower.currentPointPose.translation.x
        pathY = trajectoryFollower.currentPointPose.translation.y
        pathHdg = trajectoryFollower.currentPointPose.rotation.radians

        lookaheadX = pathX
        lookaheadY = pathY
    }

    override fun initialize() {
        notifier.startPeriodic(trajectoryFollower.dt)
    }

    override fun end() {
        synchronized(pf) {
            stopNotifier = true
            notifier.stop()
            DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = 0.0, rightOutput = 0.0)
        }
    }

    override fun isFinished() = trajectoryFollower.isFinished

    companion object {
        var pathX = 0.0
            private set
        var pathY = 0.0
            private set
        var pathHdg = 0.0
            private set

        var lookaheadX = 0.0
            private set
        var lookaheadY = 0.0
            private set
    }

    class Marker(val instance: FollowTrajectoryCommand, val t: Double)
}
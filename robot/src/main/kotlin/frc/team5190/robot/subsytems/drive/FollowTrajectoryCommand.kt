/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.math.control.VelocityPIDFController
import frc.team5190.lib.math.geometry.Pose2dWithCurvature
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.lib.math.trajectory.TrajectoryFollower
import frc.team5190.lib.math.trajectory.TrajectoryIterator
import frc.team5190.lib.math.trajectory.TrajectorySamplePoint
import frc.team5190.lib.math.trajectory.TrajectoryUtil
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.view.TimedView
import frc.team5190.robot.Constants
import frc.team5190.robot.Kinematics
import frc.team5190.robot.Localization
import frc.team5190.robot.auto.Trajectories

class FollowTrajectoryCommand(val identifier: String, pathMirrored: Boolean = false,
                              private val exit: () -> Boolean = { false }) : Command() {

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
        trajectoryFollower = TrajectoryFollower(trajectory = trajectory, dt = 0.05)

        lController = VelocityPIDFController(
                kP = Constants.kPLeftDriveVelocity / 8.0,
                kI = Constants.kILeftDriveVelocity,
                kV = Constants.kVLeftDriveVelocity,
                kS = Constants.kSLeftDriveVelocity,
                current = { DriveSubsystem.leftVelocity.FPS }
        )

        rController = VelocityPIDFController(
                kP = Constants.kPRightDriveVelocity / 8.0,
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

                val kinematics = trajectoryFollower.getRobotVelocity(Localization.robotPosition)
                val output = Kinematics.inverseKinematics(kinematics)

                DriveSubsystem.set(ControlMode.PercentOutput,
                        lController.getPIDFOutput(output.first to 0.0),
                        rController.getPIDFOutput(output.second to 0.0))

                updateDashboard()
                System.out.printf("[Trajectory Follower] X Error: %3.3f, Y Error: %3.3f, T Error: %3.3f, L: %3.3f, A: %3.3f, Actual: %3.3f%n",
                        trajectoryFollower.trajectoryPose.translation.x - Localization.robotPosition.translation.x,
                        trajectoryFollower.trajectoryPose.translation.y - Localization.robotPosition.translation.y,
                        (trajectoryFollower.trajectoryPose.rotation - Localization.robotPosition.rotation).degrees,
                        kinematics.dx, kinematics.dtheta,
                        ((DriveSubsystem.leftVelocity + DriveSubsystem.rightVelocity) / 2.0).FPS)
            }
        }
    }

    fun addMarkerAt(waypoint: Translation2d): Marker {
        // Iterate through the trajectory and add a data point every 50 ms.
        val iterator = TrajectoryIterator(TimedView(trajectory))
        val dataArray = arrayListOf<TrajectorySamplePoint<TimedState<Pose2dWithCurvature>>>()

        while (!iterator.isDone) {
            dataArray.add(iterator.advance(0.05))
        }

        return Marker(identifier, (dataArray.minBy { waypoint.distance(it.state.state.translation) }!!.state.t)
                .also { t -> println("[Trajectory Follower] Added Marker to \"$identifier\" at T = $t seconds.") })
    }

    fun hasCrossedMarker(marker: Marker): Boolean {
        return marker.identifier == this.identifier && trajectoryFollower.trajectoryPoint.state.t > marker.t
    }

    private fun updateDashboard() {
        pathX = trajectoryFollower.trajectoryPose.translation.x
        pathY = trajectoryFollower.trajectoryPose.translation.y
        pathHdg = trajectoryFollower.trajectoryPose.rotation.radians

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
            println(DriveSubsystem.leftPosition.FT)
            DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = 0.0, rightOutput = 0.0)
        }
    }

    override fun isFinished() = trajectoryFollower.isFinished || exit()

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

    class Marker(val identifier: String, val t: Double)
}
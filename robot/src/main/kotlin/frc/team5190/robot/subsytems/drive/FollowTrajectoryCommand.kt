/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.condition
import frc.team5190.lib.math.control.VelocityPIDFController
import frc.team5190.lib.math.geometry.Pose2dWithCurvature
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.lib.math.trajectory.TrajectoryIterator
import frc.team5190.lib.math.trajectory.TrajectorySamplePoint
import frc.team5190.lib.math.trajectory.TrajectoryUtil
import frc.team5190.lib.math.trajectory.followers.NonLinearController
import frc.team5190.lib.math.trajectory.followers.TrajectoryFollower
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.view.TimedView
import frc.team5190.robot.Constants
import frc.team5190.robot.Kinematics
import frc.team5190.robot.Localization
import frc.team5190.robot.auto.Trajectories

class FollowTrajectoryCommand(val identifier: String, pathMirrored: Boolean = false) : Command() {

    // Trajectory
    private var trajectory = Trajectories[identifier]

    // Path follower
    private val trajectoryFollower: TrajectoryFollower

    // PIDF controllers
    private val lController: VelocityPIDFController
    private val rController: VelocityPIDFController

    init {
        +DriveSubsystem

        if (pathMirrored) {
            trajectory = TrajectoryUtil.mirrorTimed(trajectory)
        }

        // Initialize path follower
        trajectoryFollower = NonLinearController(trajectory)

        // Initialize PIDF Controllers
        lController = VelocityPIDFController(
                kP = Constants.kPLeftDriveVelocity,
                kI = Constants.kILeftDriveVelocity,
                kV = Constants.kVLeftDriveVelocity,
                kA = Constants.kALeftDriveVelocity,
                kS = Constants.kSLeftDriveVelocity,
                current = { DriveSubsystem.leftVelocity.FPS }
        )

        rController = VelocityPIDFController(
                kP = Constants.kPRightDriveVelocity,
                kI = Constants.kIRightDriveVelocity,
                kV = Constants.kVRightDriveVelocity,
                kA = Constants.kALeftDriveVelocity,
                kS = Constants.kSRightDriveVelocity,
                current = { DriveSubsystem.rightVelocity.FPS }
        )

        // Update the frequency of the command to the follower
        updateFrequency = 250 // Hz
        finishCondition += condition { trajectoryFollower.isFinished }
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
        return marker.identifier == this.identifier && trajectoryFollower.point.state.t > marker.t
    }

    private fun updateDashboard() {
        pathX = trajectoryFollower.pose.translation.x
        pathY = trajectoryFollower.pose.translation.y
        pathHdg = trajectoryFollower.pose.rotation.radians

        lookaheadX = pathX
        lookaheadY = pathY
    }

    override suspend fun execute() {
        val kinematics = trajectoryFollower.getSteering(Localization.robotPosition)
        val output = Kinematics.inverseKinematics(kinematics)

        DriveSubsystem.set(ControlMode.PercentOutput,
                lController.getPIDFOutput(output.first to 0.0),
                rController.getPIDFOutput(output.second to 0.0))

        updateDashboard()
    }

    override suspend fun dispose() {
        DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = 0.0, rightOutput = 0.0)
        println("[Trajectory Follower] " +
                "Norm of Translational Error: " +
                "${(Localization.robotPosition.translation - trajectory.lastState.state.translation).norm}")
        println("[Trajectory Follower]" +
                "Rotation Error: " +
                "${(Localization.robotPosition.rotation - trajectory.lastState.state.rotation).degrees} degrees.")

    }

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
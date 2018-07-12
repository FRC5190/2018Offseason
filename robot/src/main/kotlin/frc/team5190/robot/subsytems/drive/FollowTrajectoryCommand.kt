/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.control.VelocityPIDFController
import frc.team5190.lib.geometry.Translation2d
import frc.team5190.lib.trajectory.TrajectoryFollower
import frc.team5190.lib.trajectory.TrajectoryUtil
import frc.team5190.robot.Constants
import frc.team5190.robot.Kinematics
import frc.team5190.robot.Localization
import frc.team5190.robot.auto.Trajectories

class FollowTrajectoryCommand(identifier: String,
                              pathMirrored: Boolean = false,
                              private val resetRobotPosition: Boolean = false) : Command() {

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
            }
        }
    }


    private fun updateDashboard() {
        pathX = trajectoryFollower.currentPointPose.translation.x
        pathY = trajectoryFollower.currentPointPose.translation.y
        pathHdg = trajectoryFollower.currentPointPose.rotation.radians

        lookaheadX = pathX
        lookaheadY = pathY
    }

    override fun initialize() {
        if (resetRobotPosition) {
            DriveSubsystem.resetEncoders()
            Localization.reset(
                    Translation2d(trajectory.firstState.state.translation.x, trajectory.firstState.state.translation.y))
        }
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
}
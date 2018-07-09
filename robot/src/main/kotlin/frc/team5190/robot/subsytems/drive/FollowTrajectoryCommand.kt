package frc.team5190.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.geometry.Translation2d
import frc.team5190.lib.pid.VelocityPIDFController
import frc.team5190.lib.trajectory.TrajectoryFollower
import frc.team5190.lib.trajectory.TrajectoryUtil
import frc.team5190.robot.Kinematics
import frc.team5190.robot.auto.Localization
import frc.team5190.robot.auto.Trajectories
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

class FollowTrajectoryCommand(file: String,
                              private val robotReversed: Boolean = false,
                              private val pathMirrored: Boolean = false,
                              private val pathReversed: Boolean = false,
                              private val resetRobotPosition: Boolean = false) : Command() {

    // Notifier objects
    private val pf = Object()
    private val notifier: Notifier
    private var stopNotifier = false

    // Trajectory
    private var trajectory = Trajectories[file]

    // Path follower
    private val trajectoryFollower: TrajectoryFollower

    // PIDF controllers
    private val lController = VelocityPIDFController()
    private val rController = VelocityPIDFController()

    init {
        requires(DriveSubsystem)

        if (pathMirrored) {
            trajectory = TrajectoryUtil.mirrorTimed(trajectory)
        }

        // Initialize path follower
        trajectoryFollower = TrajectoryFollower(trajectory = trajectory)

        // Set PIDF Values
        lController.apply {
            p = 0.08
            i = 0.01
            v = 0.05
            vIntercept = 0.1
        }
        rController.apply {
            p = 0.08
            i = 0.01
            v = 0.05
            vIntercept = 0.1
        }

        // Initialize notifier
        notifier = Notifier {
            synchronized(pf) {
                if (stopNotifier) {
                    return@Notifier
                }

                // Get left and right wheel outputs
                val output = Kinematics.inverseKinematics(
                        trajectoryFollower.getRobotVelocity(Localization.robotPosition))

                // Update PIDF controller setpoints
                val l = lController.getPIDFOutput(target = output.left, actual = DriveSubsystem.leftVelocity.FPS.value)
                val r = rController.getPIDFOutput(target = output.right, actual = DriveSubsystem.rightVelocity.FPS.value)

                // Set drive motors and update companion values
                DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = l, rightOutput = r)
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
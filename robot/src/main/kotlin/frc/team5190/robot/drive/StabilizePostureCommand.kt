package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.control.PIDFController
import frc.team5190.lib.control.PostureStabilizer
import frc.team5190.lib.math.Pose2D
import frc.team5190.robot.Kinematics
import frc.team5190.robot.Localization

class StabilizePostureCommand(finalPose: Pose2D) : Command() {

    private val ps = Object()
    private var stopNotifier = false
    private val postureStabilizer = PostureStabilizer(finalPose)

    private val lController = PIDFController().apply {
        p = 0.08
        i = 0.01
        v = 0.05
        vIntercept = 0.1
    }

    private val rController = PIDFController().apply {
        p = 0.08
        i = 0.01
        v = 0.05
        vIntercept = 0.1
    }

    private val notifier = Notifier {
        synchronized(ps) {
            if (stopNotifier) {
                return@Notifier
            }

            val output = Kinematics.inverseKinematics(
                    postureStabilizer.getRobotVelocity(Localization.robotPosition))

            // Update PIDF controller setpoints
            val l = lController.getPIDFOutput(target = output.left, actual = DriveSubsystem.leftVelocity.FPS.value)
            val r = rController.getPIDFOutput(target = output.right, actual = DriveSubsystem.rightVelocity.FPS.value)

            // Set drive motors and update companion values
            DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = l, rightOutput = r)
        }
    }

    override fun initialize() {
        notifier.startPeriodic(DT)
    }

    override fun end() {
        synchronized(ps) {
            notifier.stop()
            stopNotifier = true
            DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = 0.0, rightOutput = 0.0)
        }
    }

    override fun isFinished() = postureStabilizer.isFinished

    companion object {
        const val DT = 0.02
    }
}


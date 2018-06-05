package frc.team5190.robot.climb

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.commandGroup
import frc.team5190.robot.Controls
import frc.team5190.robot.Robot
import frc.team5190.robot.arm.ArmPosition
import frc.team5190.robot.arm.AutoArmCommand
import frc.team5190.robot.elevator.AutoElevatorCommand
import frc.team5190.robot.elevator.ElevatorPosition
import kotlin.math.absoluteValue

class WinchCommand : Command() {

    private var winchMoving = false
    private var lastPOV = -1

    init {
        requires(ClimbSubsystem)
    }

    override fun initialize() {
        ClimbSubsystem.resetEncoders()
    }

    override fun execute() {

        if (Controls.startButtonPressed) {
            Robot.INSTANCE.isClimbing = false
            cancel()
        }

        val winchSpeed = Controls.getY(GenericHID.Hand.kRight).takeIf { it.absoluteValue > 0.1 }
        winchSpeed?.let {
            ClimbSubsystem.set(ControlMode.PercentOutput, -it)
            winchMoving = true
        }

        if (winchSpeed == null && winchMoving) {
            ClimbSubsystem.set(ControlMode.PercentOutput, 0.0)
            winchMoving = false
        }

        val pov = Controls.pov
        if (lastPOV !=  pov) {
            when (pov) {
                90 -> ClimbSubsystem.set(ControlMode.MotionMagic, 47600.0)
                180 -> ClimbSubsystem.set(ControlMode.MotionMagic, 0.0)
                else -> null
            }.also { lastPOV = pov }
        }
    }

    override fun isFinished() = false
}

class IdleClimbCommand : Command() {

    init {
        requires(ClimbSubsystem)
    }

    override fun initialize() {
        ClimbSubsystem.set(ControlMode.PercentOutput, 0.0)
    }

    override fun execute() {
        if(Controls.backButtonPressed) {
            Robot.INSTANCE.isClimbing = true
            commandGroup {
                addParallel(AutoArmCommand(ArmPosition.ALL_UP))
                addParallel(AutoElevatorCommand(ElevatorPosition.INTAKE))
            }.start()
            WinchCommand().start()
        }
    }

    override fun isFinished() = false
}
package frc.team5190.robot.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.robot.Controls

class ManualArmCommand : Command() {

    init {
        requires(ArmSubsystem)
    }

    override fun execute() {
        when {
            Controls.yButton -> ArmSubsystem.set(ControlMode.PercentOutput, 0.4)
            Controls.bButton -> ArmSubsystem.set(ControlMode.PercentOutput, -0.3)

            Controls.yButtonReleased -> ArmSubsystem.set(ControlMode.MotionMagic, (ArmSubsystem.currentPosition.STU.value + 50).toDouble())
            Controls.bButtonReleased -> ArmSubsystem.set(ControlMode.MotionMagic, ArmSubsystem.currentPosition.STU.value.toDouble())
        }
    }

    override fun isFinished() = false

}
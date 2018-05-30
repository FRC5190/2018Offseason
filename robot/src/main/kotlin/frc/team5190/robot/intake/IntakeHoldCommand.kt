package frc.team5190.robot.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.robot.Controls
import frc.team5190.robot.Robot
import kotlin.math.pow

class IntakeHoldCommand : Command() {

    private var triggerState = false

    init {
        requires(IntakeSubsystem)
    }

    override fun initialize() {
        IntakeSubsystem.intakeSolenoid.set(true)
        IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)
    }

    override fun execute() {
        if (!Robot.INSTANCE.isClimbing) {
            when {
                Controls.getBumper(GenericHID.Hand.kLeft) -> {
                    IntakeCommand(IntakeDirection.IN).start()
                    triggerState = true
                }
                Controls.getTriggerAxis(GenericHID.Hand.kLeft) >= 0.1 -> {
                    IntakeCommand(IntakeDirection.OUT, speed = Controls.getTriggerAxis(GenericHID.Hand.kLeft).pow(2.0) * 0.65).start()
                }
                triggerState -> {
                    IntakeSubsystem.currentCommand?.cancel()
                    triggerState = false
                }
            }
        }
    }

    override fun isFinished() = false
}
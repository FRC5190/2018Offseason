package frc.team5190.robot.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command
import kotlin.math.absoluteValue

open class IntakeCommand(private val direction: IntakeDirection, private val timeout: Double = -.1, speed: Double = -1.0): Command() {

    private val speed = speed.takeIf { it >= 0.0 }
            ?: if (direction == IntakeDirection.IN) 1.0 else 0.65

    init {
        this.requires(IntakeSubsystem)
    }

    override fun initialize() {
        IntakeSubsystem.intakeSolenoid.set(false)

        if (timeout > 0) setTimeout(timeout)

        val motorOutput = when (direction) {
            IntakeDirection.IN -> -(speed).absoluteValue
            IntakeDirection.OUT -> speed.absoluteValue
        }

        IntakeSubsystem.set(ControlMode.PercentOutput, motorOutput)
    }

    override fun isFinished() = (timeout > 0 && isTimedOut) ||
            (direction == IntakeDirection.IN && IntakeSubsystem.isCubeIn)

}

/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command
import kotlin.math.absoluteValue

class IntakeCommand(private val direction: IntakeSubsystem.Direction,
                    private val timeout: Double = Double.POSITIVE_INFINITY,
                    private val speed: Double = 1.0,
                    private val exit: () -> Boolean = { false }) : Command() {
    init {
        requires(IntakeSubsystem)
    }

    override fun initialize() {
        IntakeSubsystem.solenoid.set(false)
        setTimeout(timeout)

        IntakeSubsystem.set(ControlMode.PercentOutput, when (direction) {
            IntakeSubsystem.Direction.IN -> speed.absoluteValue * -1.0
            IntakeSubsystem.Direction.OUT -> speed.absoluteValue
        })
    }

    override fun end() = IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)

    override fun isFinished() = isTimedOut ||
            (direction == IntakeSubsystem.Direction.IN && IntakeSubsystem.cubeIn) || exit()
}
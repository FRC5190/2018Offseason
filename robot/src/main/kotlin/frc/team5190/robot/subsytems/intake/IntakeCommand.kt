/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.TimeoutCommand
import kotlin.math.absoluteValue

class IntakeCommand(private val direction: IntakeSubsystem.Direction,
                    timeout: Long = Long.MAX_VALUE,
                    private val speed: Double = 1.0,
                    private val exit: () -> Boolean = { false }) : TimeoutCommand(timeout) {
    init {
        requires(IntakeSubsystem)
    }

    override suspend fun initialize() {
        IntakeSubsystem.solenoid.set(false)

        IntakeSubsystem.set(ControlMode.PercentOutput, when (direction) {
            IntakeSubsystem.Direction.IN -> speed.absoluteValue * -1.0
            IntakeSubsystem.Direction.OUT -> speed.absoluteValue
        })
    }

    override suspend fun dispose() = IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)

    override suspend fun isFinished() = super.isFinished() ||
            (direction == IntakeSubsystem.Direction.IN && IntakeSubsystem.cubeIn) || exit()
}
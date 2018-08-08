/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Condition
import frc.team5190.lib.commands.TimeoutCommand
import frc.team5190.lib.commands.or
import frc.team5190.lib.utils.DoubleSource
import frc.team5190.lib.utils.constSource
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class IntakeCommand(private val direction: IntakeSubsystem.Direction,
                    private val speed: DoubleSource,
                    timeout: Long = Long.MAX_VALUE,
                    exitCondition: Condition = Condition.FALSE) : TimeoutCommand(timeout, TimeUnit.MILLISECONDS) {

    constructor(direction: IntakeSubsystem.Direction,
                speed: Double = 1.0,
                timeout: Long = Long.MAX_VALUE,
                exitCondition: Condition = Condition.FALSE) : this(direction, constSource(speed), timeout, exitCondition)

    init {
        +IntakeSubsystem

        finishCondition += exitCondition or { direction == IntakeSubsystem.Direction.IN && IntakeSubsystem.cubeIn }
    }

    override suspend fun initialize() {
        super.initialize()
        IntakeSubsystem.solenoid.set(false)
        updateSpeed()
    }

    override suspend fun execute() {
        super.execute()
        updateSpeed()
    }

    private fun updateSpeed() {
        IntakeSubsystem.set(ControlMode.PercentOutput, when (direction) {
            IntakeSubsystem.Direction.IN -> speed.value.absoluteValue * -1.0
            IntakeSubsystem.Direction.OUT -> speed.value.absoluteValue
        })
    }

    override suspend fun dispose() {
        super.dispose()
        IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)
    }
}
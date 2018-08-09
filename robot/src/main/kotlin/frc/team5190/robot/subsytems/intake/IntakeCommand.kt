/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.TimeoutCommand
import frc.team5190.lib.commands.and
import frc.team5190.lib.commands.condition
import frc.team5190.lib.utils.DoubleSource
import frc.team5190.lib.utils.constSource
import frc.team5190.robot.sensors.CubeSensors
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class IntakeCommand(private val direction: IntakeSubsystem.Direction,
                    private val speed: DoubleSource = constSource(1.0),
                    timeout: Long = Long.MAX_VALUE) : TimeoutCommand(timeout, TimeUnit.MILLISECONDS) {

    init {
        +IntakeSubsystem
        updateFrequency = DEFAULT_FREQUENCY
        if(direction == IntakeSubsystem.Direction.IN) finishCondition += CubeSensors.cubeIn
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
        val newSpeed = speed.value.absoluteValue
        IntakeSubsystem.set(ControlMode.PercentOutput, when (direction) {
            IntakeSubsystem.Direction.IN -> -newSpeed
            IntakeSubsystem.Direction.OUT -> newSpeed
        })
    }

    override suspend fun dispose() {
        super.dispose()
        IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)
    }
}
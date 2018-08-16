/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.utils.DoubleSource
import frc.team5190.lib.utils.constSource
import frc.team5190.robot.sensors.CubeSensors
import kotlin.math.absoluteValue

class IntakeCommand(private val direction: IntakeSubsystem.Direction,
                    private val speed: DoubleSource = constSource(1.0)) : Command() {

    init {
        +IntakeSubsystem
        if (direction == IntakeSubsystem.Direction.IN) finishCondition += CubeSensors.cubeIn
    }

    override suspend fun initialize() {
        IntakeSubsystem.solenoid.set(false)
        updateSpeed()
    }

    override suspend fun execute() {
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
        IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)
    }
}
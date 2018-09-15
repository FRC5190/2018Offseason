/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.utils.DoubleSource
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.sensors.CubeSensors
import kotlin.math.absoluteValue

class IntakeCommand(private val direction: IntakeSubsystem.Direction,
                    private val speed: DoubleSource = Source(1.0)) : org.ghrobotics.lib.commands.Command(IntakeSubsystem) {

    init {
        if (direction == IntakeSubsystem.Direction.IN) _finishCondition += CubeSensors.cubeIn
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
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.commands.Command
import org.ghrobotics.lib.utils.DoubleSource
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.sensors.CubeSensors
import kotlin.math.withSign

class IntakeCommand(
    direction: IntakeSubsystem.Direction,
    speed: DoubleSource = Source(1.0)
) : Command(IntakeSubsystem) {

    private val speed = speed
        .withProcessing { it.withSign(if (direction == IntakeSubsystem.Direction.IN) -1 else 1) }

    constructor(direction: IntakeSubsystem.Direction, speed: Double) : this(direction, Source(speed))

    init {
        if (direction == IntakeSubsystem.Direction.IN) _finishCondition += CubeSensors.cubeIn
    }

    override suspend fun initialize() {
        IntakeSubsystem.solenoid.set(false)
    }

    override suspend fun execute() {
        IntakeSubsystem.set(ControlMode.PercentOutput, speed.value)
    }

    override suspend fun dispose() {
        IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)
    }
}
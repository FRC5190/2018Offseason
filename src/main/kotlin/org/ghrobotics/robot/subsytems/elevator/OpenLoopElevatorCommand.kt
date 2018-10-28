/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.utils.DoubleSource
import org.ghrobotics.lib.utils.Source


class OpenLoopElevatorCommand(private val percentOutput: DoubleSource) : FalconCommand(ElevatorSubsystem) {
    constructor(percentOutput: Double) : this(Source(percentOutput))

    override suspend fun execute() {
        if (ElevatorSubsystem.atBottom && !ElevatorSubsystem.reset) {
            ElevatorSubsystem.resetEncoders()
            ElevatorSubsystem.reset = true
        }
        if (ElevatorSubsystem.reset && !ElevatorSubsystem.atBottom) {
            ElevatorSubsystem.reset = false
        }
        ElevatorSubsystem.set(ControlMode.PercentOutput, percentOutput())
    }
}
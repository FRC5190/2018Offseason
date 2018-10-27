package org.ghrobotics.robot.subsytems.climber

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.utils.DoubleSource

class OpenLoopClimbCommand(private val percentOutput: DoubleSource) : FalconCommand(ClimberSubsystem) {
    override suspend fun execute() {
        ClimberSubsystem.set(ControlMode.PercentOutput, percentOutput.value)
    }
}
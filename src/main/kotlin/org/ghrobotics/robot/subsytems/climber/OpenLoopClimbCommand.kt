package org.ghrobotics.robot.subsytems.climber

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.utils.DoubleSource
import org.ghrobotics.lib.utils.Source

class OpenLoopClimbCommand(private val percentOutput: DoubleSource) : FalconCommand(ClimberSubsystem) {
    constructor(percentOutput: Double) : this(Source(percentOutput))

    override suspend fun execute() {
        ClimberSubsystem.set(ControlMode.PercentOutput, percentOutput())
    }
}
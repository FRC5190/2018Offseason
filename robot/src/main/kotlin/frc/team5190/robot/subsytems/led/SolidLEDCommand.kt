package frc.team5190.robot.subsytems.led

import frc.team5190.lib.commands.Command
import frc.team5190.lib.extensions.setLEDOutput
import frc.team5190.robot.sensors.Canifier
import java.awt.Color

class SolidLEDCommand(private val color: Color) : Command() {
    init {
        +LEDSubsystem
    }

    override suspend fun initialize() {
        Canifier.setLEDOutput(color)
    }
}
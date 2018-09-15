package org.ghrobotics.robot.subsytems.led

import org.ghrobotics.lib.utils.setLEDOutput
import org.ghrobotics.robot.sensors.Canifier
import java.awt.Color

class BlinkingLEDCommand(private val color: Color,
                         private val blinkIntervalMs: Long) : org.ghrobotics.lib.commands.Command(LEDSubsystem) {
    override suspend fun execute() {
        if (System.currentTimeMillis() % blinkIntervalMs > (blinkIntervalMs / 2)) {
            Canifier.setLEDOutput(Color.BLACK)
        } else {
            Canifier.setLEDOutput(color)
        }
    }
}
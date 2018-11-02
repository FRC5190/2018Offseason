package org.ghrobotics.robot.subsytems.led

import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.mathematics.units.Time
import org.ghrobotics.lib.utils.setLEDOutput
import org.ghrobotics.robot.sensors.Canifier
import java.awt.Color

class BlinkingLEDCommand(
    private val color: Color,
    blinkInterval: Time
) : FalconCommand(LEDSubsystem) {
    private val blinkIntervalMs = blinkInterval.millisecond.toLong()
    override suspend fun execute() {
        if (System.currentTimeMillis() % blinkIntervalMs > (blinkIntervalMs / 2)) {
            Canifier.setLEDOutput(Color.BLACK)
        } else {
            Canifier.setLEDOutput(color)
        }
    }
}
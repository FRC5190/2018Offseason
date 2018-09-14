package frc.team5190.robot.subsytems.led

import frc.team5190.lib.commands.Command
import frc.team5190.lib.utils.setLEDOutput
import frc.team5190.robot.sensors.Canifier
import java.awt.Color

class BlinkingLEDCommand(private val color: Color,
                         private val blinkIntervalMs: Long) : Command(LEDSubsystem) {
    override suspend fun execute() {
        if (System.currentTimeMillis() % blinkIntervalMs > (blinkIntervalMs / 2)) {
            Canifier.setLEDOutput(Color.BLACK)
        } else {
            Canifier.setLEDOutput(color)
        }
    }
}
package frc.team5190.robot.subsytems.led

import frc.team5190.lib.commands.TimeoutCommand
import frc.team5190.lib.extensions.setLEDOutput
import frc.team5190.robot.sensors.Canifier
import java.awt.Color
import java.util.concurrent.TimeUnit

class BlinkingLEDCommand(private val color: Color,
                         private val blinkIntervalMs: Long,
                         timeout: Long = Long.MAX_VALUE) : TimeoutCommand(timeout, TimeUnit.MILLISECONDS) {
    init {
        +LEDSubsystem
        updateFrequency = DEFAULT_FREQUENCY
    }

    override suspend fun initialize() {
        super.initialize()
        println(updateFrequency)
    }

    override suspend fun execute() {
        if (System.currentTimeMillis() % blinkIntervalMs > (blinkIntervalMs / 2)) {
            Canifier.setLEDOutput(Color.BLACK)
        } else {
            Canifier.setLEDOutput(color)
        }
    }
}
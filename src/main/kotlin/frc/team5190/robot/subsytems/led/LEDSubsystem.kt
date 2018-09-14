package frc.team5190.robot.subsytems.led

import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.commands.sequential
import frc.team5190.lib.utils.observabletype.invokeOnFalse
import frc.team5190.lib.utils.observabletype.invokeOnTrue
import frc.team5190.robot.sensors.CubeSensors
import java.awt.Color
import java.util.concurrent.TimeUnit

object LEDSubsystem : Subsystem() {

    init {
        defaultCommand = SolidLEDCommand(Color.BLACK)

        val blinkCommandGroup = sequential {
            +BlinkingLEDCommand(Color.MAGENTA, 400).withTimeout(2, TimeUnit.SECONDS)
            +SolidLEDCommand(Color.MAGENTA)
        }

        CubeSensors.cubeIn.invokeOnTrue { blinkCommandGroup.start() }
        CubeSensors.cubeIn.invokeOnFalse { blinkCommandGroup.stop() }
    }
}
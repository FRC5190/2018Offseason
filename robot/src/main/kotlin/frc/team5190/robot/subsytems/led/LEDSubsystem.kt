package frc.team5190.robot.subsytems.led

import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.extensions.sequential
import frc.team5190.robot.sensors.CubeSensors
import java.awt.Color

object LEDSubsystem : Subsystem() {

    init {
        defaultCommand = SolidLEDCommand(Color.BLACK)

        val blinkCommandGroup = sequential {
            +BlinkingLEDCommand(Color.MAGENTA, 400, 2000)
            +SolidLEDCommand(Color.MAGENTA)
        }

        CubeSensors.cubeIn.invokeOnCompletion { blinkCommandGroup.start() }
        CubeSensors.cubeInInverted.invokeOnCompletion { blinkCommandGroup.stop() }
    }
}
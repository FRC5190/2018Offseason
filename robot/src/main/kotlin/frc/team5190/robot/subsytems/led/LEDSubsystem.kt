package frc.team5190.robot.subsytems.led

import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.extensions.sequential
import frc.team5190.robot.Robot
import frc.team5190.robot.sensors.CubeSensors
import kotlinx.coroutines.experimental.NonCancellable.invokeOnCompletion
import kotlinx.coroutines.experimental.launch
import java.awt.Color

object LEDSubsystem : Subsystem() {

    private val blinkCommandGroup = sequential {
        BlinkingLEDCommand(Color.PINK, 400, 2000)
        SolidLEDCommand(Color.BLACK)
    }

    init {
        defaultCommand = SolidLEDCommand(Color.BLACK)
        CubeSensors.cubeIn.invokeOnCompletion {
            launch {
                blinkCommandGroup.start()
            }
        }
        CubeSensors.cubeInInverted.invokeOnCompletion {
            launch {
                blinkCommandGroup.stop()
            }
        }
    }
}
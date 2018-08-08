package frc.team5190.robot.subsytems.led

import frc.team5190.lib.commands.Subsystem
import frc.team5190.robot.Robot
import frc.team5190.robot.sensors.CubeSensors
import kotlinx.coroutines.experimental.NonCancellable.invokeOnCompletion
import kotlinx.coroutines.experimental.launch
import java.awt.Color

object LEDSubsystem : Subsystem() {
    init {
        defaultCommand = SolidLEDCommand(Color.BLACK)
        CubeSensors.cubeIn.invokeOnCompletion {
            if (Robot.INSTANCE.isEnabled) {
                launch {
                    val color = if (Robot.INSTANCE.isAutonomous) Color.PINK else Color.GREEN
                    BlinkingLEDCommand(color, 400, 2000).start().apply {
                        invokeOnCompletion {
                            launch { SolidLEDCommand(color).start() }
                        }
                    }
                }
            }
        }
        CubeSensors.cubeInInverted.invokeOnCompletion {
            launch {
                SolidLEDCommand(Color.BLACK).start()
            }
        }

    }
}
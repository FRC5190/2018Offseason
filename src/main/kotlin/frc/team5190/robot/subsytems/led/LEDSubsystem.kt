package frc.team5190.robot.subsytems.led

import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.commands.sequential
import frc.team5190.lib.utils.observabletype.and
import frc.team5190.lib.utils.observabletype.invokeOnFalse
import frc.team5190.lib.utils.observabletype.invokeOnTrue
import frc.team5190.lib.utils.observabletype.not
import frc.team5190.robot.Controls
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

        val cubeInAndNotClimbing = CubeSensors.cubeIn and !Controls.isClimbing

        val climbingCommand = BlinkingLEDCommand(Color.ORANGE, 500)

        cubeInAndNotClimbing.invokeOnTrue { blinkCommandGroup.start() }
        cubeInAndNotClimbing.invokeOnFalse { blinkCommandGroup.stop() }

        Controls.isClimbing.invokeOnTrue { climbingCommand.start() }
        Controls.isClimbing.invokeOnFalse { climbingCommand.stop() }
    }
}
package org.ghrobotics.robot.subsytems.led

import org.ghrobotics.lib.commands.Subsystem
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.utils.observabletype.and
import org.ghrobotics.lib.utils.observabletype.invokeOnFalse
import org.ghrobotics.lib.utils.observabletype.invokeOnTrue
import org.ghrobotics.lib.utils.observabletype.not
import org.ghrobotics.robot.Controls
import org.ghrobotics.robot.sensors.CubeSensors
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
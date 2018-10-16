package org.ghrobotics.robot.subsytems.led

import org.ghrobotics.lib.commands.Subsystem
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.utils.observabletype.and
import org.ghrobotics.lib.utils.observabletype.invokeOnFalse
import org.ghrobotics.lib.utils.observabletype.invokeOnTrue
import org.ghrobotics.lib.utils.observabletype.not
import org.ghrobotics.robot.Controls
import org.ghrobotics.robot.sensors.CubeSensors
import java.awt.Color

object LEDSubsystem : Subsystem() {

    init {
        defaultCommand = SolidLEDCommand(Color.BLACK)

        val blinkCommandGroup = sequential {
            +BlinkingLEDCommand(Color.MAGENTA, 400.millisecond).withTimeout(2.second)
            +SolidLEDCommand(Color.MAGENTA)
        }

        val cubeInAndNotClimbing = CubeSensors.cubeIn and !Controls.isClimbing

        val climbingCommand = BlinkingLEDCommand(Color.ORANGE, 500.millisecond)

        cubeInAndNotClimbing.invokeOnTrue { blinkCommandGroup.start() }
        cubeInAndNotClimbing.invokeOnFalse { blinkCommandGroup.stop() }

        Controls.isClimbing.invokeOnTrue { climbingCommand.start() }
        Controls.isClimbing.invokeOnFalse { climbingCommand.stop() }
    }
}
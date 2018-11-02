package org.ghrobotics.robot.subsytems.led

import kotlinx.coroutines.GlobalScope
import org.ghrobotics.lib.commands.FalconSubsystem
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.utils.and
import org.ghrobotics.lib.utils.launchFrequency
import org.ghrobotics.lib.utils.monitor
import org.ghrobotics.robot.Controls
import org.ghrobotics.robot.sensors.CubeSensors
import java.awt.Color

object LEDSubsystem : FalconSubsystem() {

    init {
        defaultCommand = SolidLEDCommand(Color.BLACK)
    }

    override fun lateInit() {
        val blinkCommandGroup = sequential {
            +BlinkingLEDCommand(Color.MAGENTA, 400.millisecond).withTimeout(2.second)
            +SolidLEDCommand(Color.MAGENTA)
        }

        val climbingCommand = BlinkingLEDCommand(Color.ORANGE, 500.millisecond)

        val climbingMonitor = { Controls.isClimbing }.monitor
        val cubeInMonitor = (CubeSensors.cubeIn and { !Controls.isClimbing }).monitor

        GlobalScope.launchFrequency {
            climbingMonitor.onChange {
                if (it) climbingCommand.start()
                else climbingCommand.stop()
            }
            cubeInMonitor.onChange {
                if (it) blinkCommandGroup.start()
                else blinkCommandGroup.stop()
            }
        }
    }
}
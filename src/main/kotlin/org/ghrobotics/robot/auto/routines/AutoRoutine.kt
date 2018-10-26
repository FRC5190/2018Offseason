package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.commands.Command
import org.ghrobotics.lib.commands.InstantRunnableCommand
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.Localization
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.sensors.AHRS

abstract class AutoRoutine(protected val startingPosition: Source<StartingPositions>) {

    fun create() = sequential {
        +InstantRunnableCommand {
            println("[AutoRoutine] Starting routine...")
            NetworkInterface.INSTANCE.getEntry("Reset").setBoolean(true)
            val startingPositionValue = startingPosition.value
            AHRS.angleOffset = startingPositionValue.pose.rotation
            Localization.reset(startingPositionValue.pose)
        }
        +createRoutine()
    }

    protected abstract fun createRoutine(): Command

}
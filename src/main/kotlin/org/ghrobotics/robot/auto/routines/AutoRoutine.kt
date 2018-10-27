package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.commands.InstantRunnableCommand
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.sensors.AHRS
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

abstract class AutoRoutine(protected val startingPosition: Source<StartingPositions>) {

    fun create() = sequential {
        +InstantRunnableCommand {
            println("[AutoRoutine] Starting routine...")
            NetworkInterface.INSTANCE.getEntry("Reset").setBoolean(true)
            val startingPositionValue = startingPosition.value
            AHRS.angleOffset = startingPositionValue.pose.rotation
            DriveSubsystem.localization.reset(startingPositionValue.pose)
        }
        +createRoutine()
    }

    protected abstract fun createRoutine(): FalconCommand

}
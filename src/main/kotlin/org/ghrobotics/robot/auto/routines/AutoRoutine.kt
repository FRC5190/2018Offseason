package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.commands.InstantRunnableCommand
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.Localization
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.sensors.AHRS
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

abstract class AutoRoutine(protected val startingPosition: Source<StartingPositions>) {

    fun create() = sequential {
        +InstantRunnableCommand { init0() }
        +createRoutine()
    }

    private suspend fun init0() {
        println("Started Auto Routine")
        NetworkInterface.INSTANCE.getEntry("Reset").setBoolean(true)
        DriveSubsystem.resetEncoders()
        val startingPositionValue = startingPosition.value
        AHRS.angleOffset = startingPositionValue.pose.rotation.degree
        Localization.reset(startingPositionValue.pose)
        init()
    }

    protected open fun init() {}

    protected abstract fun createRoutine(): org.ghrobotics.lib.commands.Command

}
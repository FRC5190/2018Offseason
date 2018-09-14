package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.InstantRunnableCommand
import frc.team5190.lib.commands.sequential
import frc.team5190.lib.utils.Source
import frc.team5190.robot.Localization
import frc.team5190.robot.NetworkInterface
import frc.team5190.robot.auto.StartingPositions
import frc.team5190.robot.sensors.AHRS
import frc.team5190.robot.subsytems.drive.DriveSubsystem

abstract class AutoRoutine(protected val startingPosition: Source<StartingPositions>) {

    fun create() = sequential {
        +InstantRunnableCommand { init0() }
        +createRoutine()
    }

    private suspend fun init0() {
        NetworkInterface.INSTANCE.getEntry("Reset").setBoolean(true)
        DriveSubsystem.resetEncoders()
        val startingPositionValue = startingPosition.value
        AHRS.angleOffset = startingPositionValue.pose.rotation.degrees
        Localization.reset(startingPositionValue.pose)
        init()
    }

    protected open fun init() {}

    protected abstract fun createRoutine(): Command

}
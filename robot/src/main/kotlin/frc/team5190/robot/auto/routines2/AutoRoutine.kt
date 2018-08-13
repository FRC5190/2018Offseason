package frc.team5190.robot.auto.routines2

import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.InstantRunnableCommand
import frc.team5190.lib.extensions.sequential
import frc.team5190.lib.utils.State
import frc.team5190.robot.Localization
import frc.team5190.robot.NetworkInterface
import frc.team5190.robot.auto.StartingPositions
import frc.team5190.robot.sensors.AHRS
import frc.team5190.robot.subsytems.drive.DriveSubsystem

abstract class AutoRoutine(protected val startingPosition: State<StartingPositions>) {

    fun create() = sequential {
        +InstantRunnableCommand { init0() }
        +createRoutine()
    }

    private fun init0() {
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
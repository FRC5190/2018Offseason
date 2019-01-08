package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

fun characterizationRoutine() = autoRoutine {
    +DriveSubsystem.characterizeDrive(
        Constants.kWheelRadius,
        Constants.kTrackWidth / 2.0
    )
}
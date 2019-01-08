package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.commands.PeriodicRunnableCommand
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.withEquals
import org.ghrobotics.robot.auto.Autonomous.Config.startingPosition
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.Trajectories
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

fun baselineRoutine() = autoRoutine {
    // The only reason we will ever run baseline is if encoders don't work. So it makes no sense to run a trajectory.
    +PeriodicRunnableCommand({
        DriveSubsystem.tankDrive(0.3, 0.3)
    }, { false }).withTimeout(5.second)
}
package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.withEquals
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.Trajectories
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

class RoutineBaseline(startingPosition: Source<StartingPositions>) : AutoRoutine(startingPosition) {
    override fun createRoutine() = DriveSubsystem.followTrajectory(
            Trajectories.baseline,
            startingPosition.withEquals(StartingPositions.RIGHT)
    )
}
package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.Trajectories
import org.ghrobotics.robot.subsytems.drive.FollowTrajectoryCommand

class RoutineBaseline(startingPosition: Source<StartingPositions>) : AutoRoutine(startingPosition) {
    override fun createRoutine() = FollowTrajectoryCommand(
            trajectory = Source(Trajectories.baseline),
            pathMirrored = startingPosition.withEquals(StartingPositions.RIGHT))
}
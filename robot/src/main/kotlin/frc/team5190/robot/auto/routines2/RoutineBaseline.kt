package frc.team5190.robot.auto.routines2

import frc.team5190.lib.utils.State
import frc.team5190.lib.utils.constSource
import frc.team5190.lib.utils.withEquals
import frc.team5190.robot.auto.StartingPositions
import frc.team5190.robot.auto.Trajectories
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand

class RoutineBaseline(startingPosition: State<StartingPositions>) : AutoRoutine(startingPosition) {
    override fun createRoutine() = FollowTrajectoryCommand(
            trajectory = constSource(Trajectories.baseline),
            pathMirrored = startingPosition.withEquals(StartingPositions.RIGHT))
}
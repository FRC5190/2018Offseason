/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.CommandGroup
import frc.team5190.lib.extensions.sequential
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.auto.Trajectories
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand

class RoutineBaseline(private val startingPosition: Autonomous.StartingPositions) : BaseRoutine(startingPosition) {
    override val routine: CommandGroup
        get() {
            return sequential {
                +FollowTrajectoryCommand(
                        trajectory = Trajectories.baseline,
                        pathMirrored = startingPosition == Autonomous.StartingPositions.RIGHT)
            }
        }
}
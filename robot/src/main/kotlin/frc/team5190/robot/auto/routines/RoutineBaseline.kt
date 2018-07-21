/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.extensions.sequential
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand

class RoutineBaseline(private val startingPosition: Autonomous.StartingPositions) : BaseRoutine(startingPosition) {
    override val routine: CommandGroup
        get() {
            return sequential {
                +FollowTrajectoryCommand(
                        identifier = "Baseline",
                        pathMirrored = startingPosition == Autonomous.StartingPositions.RIGHT)
            }
        }
}
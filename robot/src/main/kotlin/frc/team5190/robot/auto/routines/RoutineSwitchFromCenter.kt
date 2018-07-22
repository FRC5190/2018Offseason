/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.CommandGroup
import frc.team5190.lib.extensions.parallel
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import openrio.powerup.MatchData

class RoutineSwitchFromCenter(startingPosition: Autonomous.StartingPositions,
                              private val switchSide: MatchData.OwnedSide) : BaseRoutine(startingPosition) {
    override val routine: CommandGroup
        get() {
            val switch = if (switchSide == MatchData.OwnedSide.LEFT) {
                "Left"
            } else "Right"
            val mirrored = switchSide == MatchData.OwnedSide.RIGHT

            return parallel {
                sequential {
                    +FollowTrajectoryCommand("Center Start to $switch Switch")
                    +FollowTrajectoryCommand("Switch to Center", pathMirrored = mirrored)
                    +FollowTrajectoryCommand("Center to Pyramid")
                    +FollowTrajectoryCommand("Pyramid to Center")
                    +FollowTrajectoryCommand("Center to Switch", pathMirrored = mirrored)
                }
                sequential {
                    // TODO
                }
            }
        }
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.extensions.sequential
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.Localization
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import openrio.powerup.MatchData

class RoutineScaleFromSide(private val startingPosition: Autonomous.StartingPositions,
                           private val scaleSide: MatchData.OwnedSide) : BaseRoutine() {

    override val routine: CommandGroup
        get() {
            val scale = if (startingPosition.name.first().toUpperCase() == scaleSide.name.first().toUpperCase()) {
                "Near"
            } else "Far"
            val mirrored = scaleSide == MatchData.OwnedSide.RIGHT

            Localization.reset(startingPosition.pose)

            return sequential {
                // 1st Cube in Scale
                parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Left Start to $scale Scale",
                            pathMirrored = startingPosition == Autonomous.StartingPositions.RIGHT,
                            resetRobotPosition = true))
                }

                // Pick Up 2nd Cube
                parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Scale to Cube 1",
                            pathMirrored = mirrored))
                }

                // 2nd Cube in Scale
                parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Cube 1 to Scale",
                            pathMirrored = mirrored
                    ))
                }

                // Pick Up 3rd Cube
                parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Scale to Cube 2",
                            pathMirrored = mirrored
                    ))
                }

                // 3rd Cube in Scale
                parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Cube 2 to Scale",
                            pathMirrored = mirrored
                    ))
                }

            }
        }

}
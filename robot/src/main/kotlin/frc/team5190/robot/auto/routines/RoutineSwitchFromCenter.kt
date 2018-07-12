/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.extensions.sequential
import frc.team5190.robot.Localization
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import openrio.powerup.MatchData

class RoutineSwitchFromCenter(private val switchSide: MatchData.OwnedSide) : BaseRoutine() {
    override val routine: CommandGroup
        get() {
            val switch = if (switchSide == MatchData.OwnedSide.LEFT) {
                "Left"
            } else "Right"
            val mirrored = switchSide == MatchData.OwnedSide.RIGHT

            Localization.reset(Autonomous.StartingPositions.CENTER.pose)

            return sequential {
                // 1st Cube in Switch
                add(parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Center Start to $switch Switch",
                            pathMirrored = false,
                            resetRobotPosition = true))
                })

                // Come Back to Center
                add(parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Switch to Center",
                            pathMirrored = mirrored
                    ))
                })

                // Pick Up 2nd Cube
                add(parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Center to Pyramid"
                    ))
                })

                // Come Back to Center
                add(parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Pyramid to Center"
                    ))
                })

                // 2nd Cube in Switch
                add(parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Center to Switch",
                            pathMirrored = mirrored
                    ))
                })
            }
        }
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import edu.wpi.first.wpilibj.command.CommandGroup
import edu.wpi.first.wpilibj.command.PrintCommand
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.extensions.sequential
import frc.team5190.lib.geometry.Translation2d
import frc.team5190.lib.util.StateCommand
import frc.team5190.robot.Localization
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import openrio.powerup.MatchData

class RoutineSwitchScaleFromCenter(private val switchSide: MatchData.OwnedSide,
                                   private val scaleSide: MatchData.OwnedSide) : BaseRoutine() {
    override val routine: CommandGroup
        get() {
            val switch = if (switchSide == MatchData.OwnedSide.LEFT) {
                "Left"
            } else "Right"
            val switchMirrored = switchSide == MatchData.OwnedSide.RIGHT
            val scaleMirorred = scaleSide == MatchData.OwnedSide.RIGHT

            Localization.reset(Autonomous.StartingPositions.CENTER.pose)

            val drop2ndCube  = FollowTrajectoryCommand(identifier = "Pyramid to Scale", pathMirrored = scaleMirorred)
            val shoot2ndCube = drop2ndCube.addMarkerAt(Translation2d(22.3, 20.6))

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
                            pathMirrored = switchMirrored
                    ))
                })

                // Pick Up 2nd Cube
                add(parallel {
                    add(FollowTrajectoryCommand(
                            identifier = "Center to Pyramid"
                    ))
                })

                // 2nd Cube in Scale
                add(parallel {
                    add(drop2ndCube)
                    add(sequential {
                        add(StateCommand { drop2ndCube.hasCrossedMarker(shoot2ndCube) })
                        add(PrintCommand("Has Crossed Marker 1"))
                    })
                })
            }
        }

}
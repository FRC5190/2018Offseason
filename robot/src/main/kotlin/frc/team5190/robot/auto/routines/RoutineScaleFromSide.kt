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

class RoutineScaleFromSide(private val startingPosition: Autonomous.StartingPositions,
                           private val scaleSide: MatchData.OwnedSide) : BaseRoutine() {

    override val routine: CommandGroup
        get() {
            val scale = if (startingPosition.name.first().toUpperCase() == scaleSide.name.first().toUpperCase()) {
                "Near"
            } else "Far"
            val mirrored = scaleSide == MatchData.OwnedSide.RIGHT

            Localization.reset(startingPosition.pose)

            val drop1stCube = FollowTrajectoryCommand(
                    identifier = "Left Start to $scale Scale",
                    pathMirrored = startingPosition == Autonomous.StartingPositions.RIGHT,
                    resetRobotPosition = true)

            val pickup2ndCube = FollowTrajectoryCommand(identifier = "Scale to Cube 1", pathMirrored = mirrored)
            val drop2ndCube   = FollowTrajectoryCommand(identifier = "Cube 1 to Scale", pathMirrored = mirrored)
            val pickup3rdCube = FollowTrajectoryCommand(identifier = "Scale to Cube 2", pathMirrored = mirrored)
            val drop3rdCube   = FollowTrajectoryCommand(identifier = "Cube 2 to Scale", pathMirrored = mirrored)

            val shoot1stCube  = drop1stCube.addMarkerAt(Translation2d(22.3, 20.6))
            val shoot2ndCube  = drop2ndCube.addMarkerAt(Translation2d(22.5, 19.9))
            val shoot3rdCube  = drop3rdCube.addMarkerAt(Translation2d(22.5, 19.9))

            return sequential {
                // 1st Cube in Scale
                add(parallel {
                    add(drop1stCube)
                    add(sequential {
                        add(StateCommand{ drop1stCube.hasCrossedMarker(shoot1stCube) })
                        add(PrintCommand("Has Crossed Marker 1"))
                    })
                })

                // Pick Up 2nd Cube
                add(parallel {
                    add(pickup2ndCube)
                })

                // 2nd Cube in Scale
                add(parallel {
                    add(drop2ndCube)
                    add(sequential {
                        add(StateCommand{ drop2ndCube.hasCrossedMarker(shoot2ndCube) })
                        add(PrintCommand("Has Crossed Marker 2"))
                    })
                })

                // Pick Up 3rd Cube
                add(parallel {
                    add(pickup3rdCube)
                })

                // 3rd Cube in Scale
                add(parallel {
                    add(drop3rdCube)
                    add(sequential {
                        add(StateCommand{ drop3rdCube.hasCrossedMarker(shoot3rdCube) })
                        add(PrintCommand("Has Crossed Marker 3"))
                    })
                })

            }
        }

}
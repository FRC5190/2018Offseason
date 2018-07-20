/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.commands.StateCommand
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.SubsystemPresetCommand
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import frc.team5190.robot.subsytems.intake.IntakeCommand
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import openrio.powerup.MatchData

class RoutineScaleFromSide(private val startingPosition: Autonomous.StartingPositions,
                           private val scaleSide: MatchData.OwnedSide) : BaseRoutine(startingPosition) {

    @Suppress("UNUSED_VARIABLE")
    override val routine: CommandGroup
        get() {
            val scale = if (startingPosition.name.first().toUpperCase() == scaleSide.name.first().toUpperCase()) {
                "Near"
            } else "Far"
            val mirrored = scaleSide == MatchData.OwnedSide.RIGHT

            val drop1stCube = FollowTrajectoryCommand(
                    identifier = "Left Start to $scale Scale",
                    pathMirrored = startingPosition == Autonomous.StartingPositions.RIGHT)

            val pickup2ndCube = FollowTrajectoryCommand(identifier = "Scale to Cube 1", pathMirrored = mirrored)
            val drop2ndCube   = FollowTrajectoryCommand(identifier = "Cube 1 to Scale", pathMirrored = mirrored)
            val pickup3rdCube = FollowTrajectoryCommand(identifier = "Scale to Cube 2", pathMirrored = mirrored)
            val drop3rdCube   = FollowTrajectoryCommand(identifier = "Cube 2 to Scale", pathMirrored = mirrored)
            val pickup4thCube = FollowTrajectoryCommand(identifier = "Scale to Cube 3", pathMirrored = mirrored)
            val drop4thCube   = FollowTrajectoryCommand(identifier = "Cube 3 to Scale", pathMirrored = mirrored)

            val elevatorUp   = drop1stCube.addMarkerAt(Translation2d(11.5, 23.1))
            val shoot1stCube = drop1stCube.addMarkerAt(Translation2d(22.3, 20.6))
            val shoot2ndCube = drop2ndCube.addMarkerAt(Translation2d(22.5, 19.9))
            val shoot3rdCube = drop3rdCube.addMarkerAt(Translation2d(22.5, 19.9))
            val shoot4thCube = drop4thCube.addMarkerAt(Translation2d(22.5, 19.9))

            return parallel {
                sequential {
                    add(drop1stCube)
                    add(pickup2ndCube)
                    add(drop2ndCube)
                    add(pickup3rdCube)
                    add(drop3rdCube)
                    add(pickup4thCube)
                    add(drop4thCube)
                }
                sequential {
                     add(StateCommand { drop1stCube.hasCrossedMarker(elevatorUp) || drop1stCube.isCompleted })
                     add(SubsystemPresetCommand(SubsystemPresetCommand.Preset.BEHIND) { drop1stCube.isCompleted })
                     add(StateCommand { drop1stCube.hasCrossedMarker(shoot1stCube) || drop1stCube.isCompleted })
                     add(IntakeCommand(IntakeSubsystem.Direction.OUT) { drop1stCube.isCompleted })

                     parallel {
                          add(SubsystemPresetCommand(SubsystemPresetCommand.Preset.INTAKE) { pickup2ndCube.isCompleted })
                          add(IntakeCommand(IntakeSubsystem.Direction.IN) { pickup2ndCube.isCompleted })
                     }

                     add(SubsystemPresetCommand(SubsystemPresetCommand.Preset.BEHIND) { drop2ndCube.isCompleted })
                     add(StateCommand { drop2ndCube.hasCrossedMarker(shoot2ndCube) })
                     add(IntakeCommand(IntakeSubsystem.Direction.OUT) { drop2ndCube.isCompleted })

                     parallel {
                          add(SubsystemPresetCommand(SubsystemPresetCommand.Preset.INTAKE) { pickup3rdCube.isCompleted })
                          add(IntakeCommand(IntakeSubsystem.Direction.IN) { pickup3rdCube.isCompleted })
                     }

                     add(SubsystemPresetCommand(SubsystemPresetCommand.Preset.BEHIND) { drop3rdCube.isCompleted })
                     add(StateCommand { drop2ndCube.hasCrossedMarker(shoot3rdCube) })
                     add(IntakeCommand(IntakeSubsystem.Direction.OUT) { drop3rdCube.isCompleted })

                     parallel {
                          add(SubsystemPresetCommand(SubsystemPresetCommand.Preset.INTAKE) { pickup4thCube.isCompleted })
                          add(IntakeCommand(IntakeSubsystem.Direction.IN) { pickup4thCube.isCompleted })
                     }

                     add(SubsystemPresetCommand(SubsystemPresetCommand.Preset.BEHIND) { drop4thCube.isCompleted })
                     add(StateCommand { drop4thCube.hasCrossedMarker(shoot4thCube) })
                     add(IntakeCommand(IntakeSubsystem.Direction.OUT) { drop4thCube.isCompleted })
                }
            }
        }
}
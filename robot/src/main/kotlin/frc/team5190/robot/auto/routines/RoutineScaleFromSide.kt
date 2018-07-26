/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.CommandGroup
import frc.team5190.lib.commands.ConditionCommand
import frc.team5190.lib.commands.condition
import frc.team5190.lib.commands.or
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.SubsystemPreset
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
            val shoot1stCube = drop1stCube.addMarkerAt((Translation2d(22.3, 20.6)).let { if (mirrored) it.mirror else it})
            val shoot2ndCube = drop2ndCube.addMarkerAt(Translation2d(22.5, 19.9))
            val shoot3rdCube = drop3rdCube.addMarkerAt(Translation2d(22.5, 19.9))
            val shoot4thCube = drop4thCube.addMarkerAt(Translation2d(22.5, 19.9))

            return parallel {
                sequential {
                    +drop1stCube
//                    +ConditionCommand { ElevatorSubsystem.currentPosition <= ElevatorSubsystem.Position.FSTAGE.distance }
                    +pickup2ndCube
//                    +ConditionCommand { ElevatorSubsystem.currentPosition >= ElevatorSubsystem.Position.SWITCH.distance }
                    +drop2ndCube
                    +pickup3rdCube
                    +drop3rdCube
                    +pickup4thCube
                    +drop4thCube
                }
                sequential {
                    +ConditionCommand(condition { drop1stCube.hasCrossedMarker(elevatorUp) } or drop1stCube)
                    +SubsystemPresetCommand(SubsystemPreset.BEHIND, condition(drop1stCube))
                    +ConditionCommand(condition { drop1stCube.hasCrossedMarker(shoot1stCube) } or drop1stCube)
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, exitCondition = condition(drop1stCube))

                    parallel {
                        +SubsystemPresetCommand(SubsystemPreset.INTAKE, condition(pickup2ndCube))
                        +IntakeCommand(IntakeSubsystem.Direction.IN, exitCondition = condition(pickup2ndCube))
                    }

                    +SubsystemPresetCommand(SubsystemPreset.BEHIND, condition(drop2ndCube))
                    +ConditionCommand(condition { drop2ndCube.hasCrossedMarker(shoot2ndCube) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, exitCondition = condition(drop2ndCube))

                    parallel {
                        +SubsystemPresetCommand(SubsystemPreset.INTAKE, condition(pickup3rdCube))
                        +IntakeCommand(IntakeSubsystem.Direction.IN, exitCondition = condition(pickup3rdCube))
                    }

                    +SubsystemPresetCommand(SubsystemPreset.BEHIND, condition(drop3rdCube))
                    +ConditionCommand(condition { drop2ndCube.hasCrossedMarker(shoot3rdCube) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, exitCondition = condition(drop3rdCube))

                    parallel {
                        +SubsystemPresetCommand(SubsystemPreset.INTAKE, condition(pickup4thCube))
                        +IntakeCommand(IntakeSubsystem.Direction.IN, exitCondition = condition(pickup4thCube))
                    }

                    +SubsystemPresetCommand(SubsystemPreset.BEHIND, condition(drop4thCube))
                    +ConditionCommand(condition { drop4thCube.hasCrossedMarker(shoot4thCube) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, exitCondition = condition(drop4thCube))
                }
            }
        }
}
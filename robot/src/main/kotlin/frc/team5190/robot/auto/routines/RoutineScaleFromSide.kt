/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.*
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.SubsystemPreset
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.arm.ClosedLoopArmCommand
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import frc.team5190.robot.subsytems.elevator.ClosedLoopElevatorCommand
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.intake.IntakeCommand
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import openrio.powerup.MatchData
import java.util.concurrent.TimeUnit

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

            val after2ndCube = DelayCommand(250, TimeUnit.MILLISECONDS)

            val elevatorUp   = drop1stCube.addMarkerAt(Translation2d(11.0, 23.1))
            val shoot1stCube = drop1stCube.addMarkerAt(if (mirrored) Translation2d(22.3, 20.6) else Translation2d(19.0, 8.0))
            val shoot2ndCube = drop2ndCube.addMarkerAt(Translation2d(22.5, 19.9))
            val shoot3rdCube = drop3rdCube.addMarkerAt(Translation2d(22.5, 19.9))

            return parallel {
                sequential {
                    +drop1stCube
                    +ConditionCommand(condition { ElevatorSubsystem.currentPosition < ElevatorSubsystem.Position.FSTAGE.distance})
                    +pickup2ndCube
                    +after2ndCube
                    +drop2ndCube
                    +pickup3rdCube
                    +drop3rdCube
                    +pickup4thCube

                }
                sequential {
                    +DelayCommand(250, TimeUnit.MILLISECONDS)

                    parallel {
                        +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.SWITCH)
                        +ClosedLoopArmCommand(ArmSubsystem.Position.UP)
                    }.withTimeout(1, TimeUnit.SECONDS)

                    +ConditionCommand(condition { drop1stCube.hasCrossedMarker(elevatorUp) } or drop1stCube)
                    +SubsystemPreset.BEHIND.command.withExit(condition(drop1stCube))
                    +ConditionCommand(condition { drop1stCube.hasCrossedMarker(shoot1stCube) } or drop1stCube)
                    +IntakeCommand(IntakeSubsystem.Direction.OUT).withExit(condition(drop1stCube))

                    parallel {
                        +SubsystemPreset.INTAKE.command.withExit(condition(pickup2ndCube))
                        +IntakeCommand(IntakeSubsystem.Direction.IN).withExit(condition(pickup2ndCube))
                    }

                    +SubsystemPreset.BEHIND.command.withExit(condition(drop2ndCube))
                    +ConditionCommand(condition { drop2ndCube.hasCrossedMarker(shoot2ndCube) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT).withExit(condition(drop2ndCube))

                    parallel {
                        +SubsystemPreset.INTAKE.command.withExit(condition(pickup3rdCube))
                        +IntakeCommand(IntakeSubsystem.Direction.IN).withExit(condition(pickup3rdCube))
                    }

                    +SubsystemPreset.BEHIND.command.withExit(condition(drop3rdCube))
                    +ConditionCommand(condition { drop3rdCube.hasCrossedMarker(shoot3rdCube) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT).withTimeout(500L)

                    parallel {
                        +SubsystemPreset.INTAKE.command.withExit(condition(pickup4thCube))
                        +IntakeCommand(IntakeSubsystem.Direction.IN).withExit(condition(pickup4thCube))
                    }
                }
            }
        }
}
package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.commands.*
import org.ghrobotics.lib.mathematics.units.NativeUnits
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.map
import org.ghrobotics.lib.utils.mergeSource
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.Trajectories
import org.ghrobotics.robot.sensors.CubeSensors
import org.ghrobotics.robot.subsytems.SubsystemPreset
import org.ghrobotics.robot.subsytems.arm.ArmSubsystem
import org.ghrobotics.robot.subsytems.arm.ClosedLoopArmCommand
import org.ghrobotics.robot.subsytems.drive.FollowTrajectoryCommand
import org.ghrobotics.robot.subsytems.elevator.ClosedLoopElevatorCommand
import org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem
import org.ghrobotics.robot.subsytems.intake.IntakeCommand
import org.ghrobotics.robot.subsytems.intake.IntakeSubsystem
import openrio.powerup.MatchData
import java.util.concurrent.TimeUnit

class RoutineScaleFromSide(startingPosition: Source<StartingPositions>,
                           private val scaleSide: Source<MatchData.OwnedSide>) : AutoRoutine(startingPosition) {

    override fun createRoutine(): org.ghrobotics.lib.commands.Command {
        val cross = mergeSource(startingPosition, scaleSide) { one, two -> !one.name.first().equals(two.name.first(), true) }
        val shouldMirrorPath = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

        val drop1stCube = FollowTrajectoryCommand(
                trajectory = cross.map(Trajectories.leftStartToFarScale, Trajectories.leftStartToNearScale),
                pathMirrored = startingPosition.withEquals(StartingPositions.RIGHT))

        val pickup2ndCube = FollowTrajectoryCommand(Trajectories.scaleToCube1, shouldMirrorPath)
        val drop2ndCube = FollowTrajectoryCommand(Trajectories.cube1ToScale, shouldMirrorPath)
        val pickup3rdCube = FollowTrajectoryCommand(Trajectories.scaleToCube2, shouldMirrorPath)
        val drop3rdCube = FollowTrajectoryCommand(Trajectories.cube2ToScale, shouldMirrorPath)
        val pickup4thCube = FollowTrajectoryCommand(Trajectories.scaleToCube3, shouldMirrorPath)

        val timeToGoUp = cross.map(1.50, 1.75).value
        val outtakeSpeed = cross.map(0.65, 0.35).value

        return sequential {

            var start = 0L

            parallel {
                +drop1stCube
                sequential {
                    +DelayCommand(500, TimeUnit.MILLISECONDS)

                    parallel {
                        +InstantRunnableCommand { start = System.currentTimeMillis() }
                        +ClosedLoopArmCommand(ArmSubsystem.kUpPosition)
                        +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition)
                    }.overrideExit(UpdatableObservableValue {
                        (System.currentTimeMillis() - start) > (drop1stCube.trajectory.value.lastState.t - timeToGoUp)
                                .coerceAtLeast(0.001) * 1000
                    })

                    parallel {
                        +SubsystemPreset.BEHIND.command
                        sequential {
                            +ConditionCommand(UpdatableObservableValue { ArmSubsystem.currentPosition > ArmSubsystem.kBehindPosition - NativeUnits(100) })
                            +DelayCommand(100, TimeUnit.MILLISECONDS)
                            +IntakeCommand(IntakeSubsystem.Direction.OUT, Source(outtakeSpeed)).withTimeout(500, TimeUnit.MILLISECONDS)
                        }
                    }
                }
            }
            parallel {
                +SubsystemPreset.INTAKE.command
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(10L, TimeUnit.SECONDS)
                sequential {
                    +DelayCommand(300, TimeUnit.MILLISECONDS)
                    +pickup2ndCube.withExit(CubeSensors.cubeIn)
                }
            }
            parallel {
                +drop2ndCube.withExit(UpdatableObservableValue {
                    (ElevatorSubsystem.currentPosition > ElevatorSubsystem.kFirstStagePosition
                            && !CubeSensors.cubeIn.value &&
                            ArmSubsystem.currentPosition > ArmSubsystem.kBehindPosition - NativeUnits(100))
                })
                sequential {
                    +DelayCommand(((drop2ndCube.trajectory.value.lastState.t - 2.7) * 1000).toLong(), TimeUnit.MILLISECONDS)
                    +SubsystemPreset.BEHIND.command
                }
                sequential {
                    +ConditionCommand(UpdatableObservableValue { ArmSubsystem.currentPosition > ArmSubsystem.kBehindPosition - NativeUnits(100) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, Source(0.4)).withTimeout(500, TimeUnit.MILLISECONDS)
                }
            }
            parallel {
                +SubsystemPreset.INTAKE.command
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(10L, TimeUnit.SECONDS)
                +pickup3rdCube.withExit(CubeSensors.cubeIn)
            }
            parallel {
                +drop3rdCube.withExit(UpdatableObservableValue {
                    (ElevatorSubsystem.currentPosition > ElevatorSubsystem.kFirstStagePosition
                            && !CubeSensors.cubeIn.value &&
                            ArmSubsystem.currentPosition > ArmSubsystem.kBehindPosition - NativeUnits(100))
                })
                sequential {
                    +DelayCommand(((drop3rdCube.trajectory.value.lastState.t - 2.7) * 1000).toLong(), TimeUnit.MILLISECONDS)
                    +SubsystemPreset.BEHIND.command
                }
                sequential {
                    +ConditionCommand(UpdatableObservableValue { ArmSubsystem.currentPosition > ArmSubsystem.kBehindPosition - NativeUnits(100) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, Source(0.4)).withTimeout(500, TimeUnit.MILLISECONDS)
                }
            }
            parallel {
                +SubsystemPreset.INTAKE.command
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(10L, TimeUnit.SECONDS)
                +pickup4thCube.withExit(CubeSensors.cubeIn)
            }
        }
    }
}
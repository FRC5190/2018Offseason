package org.ghrobotics.robot.auto.routines

import kotlinx.coroutines.experimental.GlobalScope
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.ConditionCommand
import org.ghrobotics.lib.commands.DelayCommand
import org.ghrobotics.lib.commands.parallel
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.map
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.auto.Autonomous
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

class RoutineScaleFromSide(
    startingPosition: Source<StartingPositions>,
    private val scaleSide: Source<MatchData.OwnedSide>
) : AutoRoutine(startingPosition) {

    override fun createRoutine(): org.ghrobotics.lib.commands.Command {
        val shouldMirrorPath = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

        val drop1stCube = FollowTrajectoryCommand(
            trajectory = Autonomous.isSameSide.map(
                Trajectories.leftStartToNearScale,
                Trajectories.leftStartToFarScale
            ),
            pathMirrored = startingPosition.withEquals(StartingPositions.RIGHT)
        )

        val pickup2ndCube = FollowTrajectoryCommand(Trajectories.scaleToCube1, shouldMirrorPath)
        val drop2ndCube = FollowTrajectoryCommand(Trajectories.cube1ToScale, shouldMirrorPath)
        val pickup3rdCube = FollowTrajectoryCommand(Trajectories.scaleToCube2, shouldMirrorPath)
        val drop3rdCube = FollowTrajectoryCommand(Trajectories.cube2ToScale, shouldMirrorPath)
        val pickup4thCube = FollowTrajectoryCommand(Trajectories.scaleToCube3, shouldMirrorPath)

        val timeToGoUp = Autonomous.isSameSide.map(
            2.75.second,
            2.50.second
        ).withProcessing { drop1stCube.trajectory.value.lastState.t.second - it }

        val outtakeSpeed = Autonomous.isSameSide.map(0.35, 0.65)

        return sequential {
            +parallel {
                +drop1stCube.withExit(GlobalScope.updatableValue {
                    (ElevatorSubsystem.elevatorPosition > ElevatorSubsystem.kFirstStagePosition
                            && !CubeSensors.cubeIn.value
                            && ArmSubsystem.armPosition > Constants.kArmBehindPosition - Constants.kArmAutoTolerance)
                })
                +sequential {
                    +DelayCommand(500.millisecond)

                    +parallel {
                        +ClosedLoopArmCommand(Constants.kArmUpPosition)
                        +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition)
                    }

                    +sequential {
                        +DelayCommand(timeToGoUp)
                        +parallel {
                            +sequential {
                                +ConditionCommand(GlobalScope.updatableValue {
                                    ArmSubsystem.armPosition > Constants.kArmBehindPosition - Constants.kArmAutoTolerance
                                })
                                if (!Autonomous.isSameSide.value) +DelayCommand(0.1.second)
                                +DelayCommand(100.millisecond)
                                +IntakeCommand(IntakeSubsystem.Direction.OUT, outtakeSpeed).withTimeout(500.millisecond)
                            }
                        }
                    }
                }
            }
            +parallel {
                +SubsystemPreset.INTAKE.command
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(10.second)
                +sequential {
                    +DelayCommand(300.millisecond)
                    +pickup2ndCube.withExit(CubeSensors.cubeIn)
                }
            }
            +parallel {
                +drop2ndCube.withExit(GlobalScope.updatableValue {
                    (ElevatorSubsystem.elevatorPosition > ElevatorSubsystem.kFirstStagePosition
                            && !CubeSensors.cubeIn.value
                            && ArmSubsystem.armPosition > Constants.kArmBehindPosition - Constants.kArmAutoTolerance)
                })
                +sequential {
                    +DelayCommand((drop2ndCube.trajectory.value.lastState.t - 2.7).second)
                    +SubsystemPreset.BEHIND.command
                }
                +sequential {
                    +ConditionCommand(GlobalScope.updatableValue {
                        ArmSubsystem.armPosition > Constants.kArmBehindPosition - Constants.kArmAutoTolerance
                    })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, 0.4).withTimeout(500.millisecond)
                }
            }
            +parallel {
                +SubsystemPreset.INTAKE.command
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(10.second)
                +pickup3rdCube.withExit(CubeSensors.cubeIn)
            }
            +parallel {
                +drop3rdCube.withExit(GlobalScope.updatableValue {
                    (ElevatorSubsystem.elevatorPosition > ElevatorSubsystem.kFirstStagePosition
                            && !CubeSensors.cubeIn.value
                            && ArmSubsystem.armPosition > Constants.kArmBehindPosition - Constants.kArmAutoTolerance)
                })
                +sequential {
                    +DelayCommand((drop3rdCube.trajectory.value.lastState.t - 2.7).second)
                    +SubsystemPreset.BEHIND.command
                }
                +sequential {
                    +ConditionCommand(GlobalScope.updatableValue {
                        ArmSubsystem.armPosition > Constants.kArmBehindPosition - Constants.kArmAutoTolerance
                    })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, 0.4).withTimeout(500.millisecond)
                }
            }
            +parallel {
                +SubsystemPreset.INTAKE.command
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(10.second)
                +pickup4thCube.withExit(CubeSensors.cubeIn)
            }
        }
    }
}
package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.commands.ConditionCommand
import org.ghrobotics.lib.commands.DelayCommand
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.mathematics.units.NativeUnits
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.map
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.Trajectories
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

class RoutineSwitchScaleFromCenter(startingPosition: Source<StartingPositions>,
                                   private val switchSide: Source<MatchData.OwnedSide>,
                                   private val scaleSide: Source<MatchData.OwnedSide>) : AutoRoutine(startingPosition) {
    override fun createRoutine(): org.ghrobotics.lib.commands.Command {
        val switch = switchSide.withEquals(MatchData.OwnedSide.LEFT)
        val switchMirrored = switchSide.withEquals(MatchData.OwnedSide.RIGHT)
        val scaleMirrored = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

        val drop1stCube = FollowTrajectoryCommand(switch.map(Trajectories.centerStartToLeftSwitch, Trajectories.centerStartToRightSwitch))
        val toCenter = FollowTrajectoryCommand(Trajectories.switchToCenter, switchMirrored)
        val toPyramid = FollowTrajectoryCommand(Trajectories.centerToPyramid)
        val drop2ndCube = FollowTrajectoryCommand(Trajectories.pyramidToScale, scaleMirrored)

        return sequential {
            parallel {
                +drop1stCube
                +SubsystemPreset.SWITCH.command
                sequential {
                    +DelayCommand(((drop1stCube.trajectory.value.lastState.t - 0.2) * 1000).toLong(), TimeUnit.MILLISECONDS)
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, Source(0.5)).withTimeout(200, TimeUnit.MILLISECONDS)
                }
            }
            parallel {
                +toCenter
                sequential {
                    +DelayCommand(500, TimeUnit.MILLISECONDS)
                    +SubsystemPreset.INTAKE.command
                }
            }
            parallel {
                +toPyramid
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(3L, TimeUnit.SECONDS)
            }
            parallel {
                +drop2ndCube
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition)
                +ClosedLoopArmCommand(ArmSubsystem.kUpPosition)
                sequential {
                    +DelayCommand(((drop2ndCube.trajectory.value.lastState.t - 1.75) * 1000).toLong(), TimeUnit.MILLISECONDS)
                    +SubsystemPreset.BEHIND.command
                    +ConditionCommand(UpdatableObservableValue { ArmSubsystem.currentPosition > ArmSubsystem.kBehindPosition - NativeUnits(100) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, Source(0.35)).withTimeout(500, TimeUnit.MILLISECONDS)
                }
            }
        }
    }
}
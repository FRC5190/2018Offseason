package org.ghrobotics.robot.auto.routines

import kotlinx.coroutines.experimental.GlobalScope
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.*
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.map
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants
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

class RoutineSwitchScaleFromCenter(
    startingPosition: Source<StartingPositions>,
    private val switchSide: Source<MatchData.OwnedSide>,
    private val scaleSide: Source<MatchData.OwnedSide>
) : AutoRoutine(startingPosition) {
    override fun createRoutine(): AbstractFalconCommand {
        val switch = switchSide.withEquals(MatchData.OwnedSide.LEFT)
        val switchMirrored = switchSide.withEquals(MatchData.OwnedSide.RIGHT)
        val scaleMirrored = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

        val drop1stCube = FollowTrajectoryCommand(
            switch.map(
                Trajectories.centerStartToLeftSwitch,
                Trajectories.centerStartToRightSwitch
            )
        )
        val toCenter = FollowTrajectoryCommand(Trajectories.switchToCenter, switchMirrored)
        val toPyramid = FollowTrajectoryCommand(Trajectories.centerToPyramid)
        val drop2ndCube = FollowTrajectoryCommand(Trajectories.pyramidToScale, scaleMirrored)

        return sequential {
            +parallel {
                +drop1stCube
                +SubsystemPreset.SWITCH.command
                +sequential {
                    +DelayCommand((drop1stCube.trajectory.value.lastState.t - 0.2).second)
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, 0.5).withTimeout(200.millisecond)
                }
            }
            +parallel {
                +toCenter
                +sequential {
                    +DelayCommand(500.millisecond)
                    +SubsystemPreset.INTAKE.command
                }
            }
            +parallel {
                +toPyramid
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(3.second)
            }
            +parallel {
                +drop2ndCube
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition)
                +ClosedLoopArmCommand(Constants.kArmUpPosition)
                sequential {
                    +DelayCommand((drop2ndCube.trajectory.value.lastState.t - 1.75).second)
                    +SubsystemPreset.BEHIND.command
                    +ConditionCommand(GlobalScope.updatableValue {
                        ArmSubsystem.armPosition > Constants.kArmBehindPosition - Constants.kArmAutoTolerance
                    })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, 0.35).withTimeout(500.millisecond)
                }
            }
        }
    }
}
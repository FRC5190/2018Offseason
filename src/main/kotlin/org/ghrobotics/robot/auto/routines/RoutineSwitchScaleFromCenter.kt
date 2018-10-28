package org.ghrobotics.robot.auto.routines

import kotlinx.coroutines.experimental.GlobalScope
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.*
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.map
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.lib.utils.withEquals
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.Trajectories
import org.ghrobotics.robot.auto.Trajectories.centerStartToLeftSwitch
import org.ghrobotics.robot.auto.Trajectories.centerStartToRightSwitch
import org.ghrobotics.robot.auto.Trajectories.pyramidToScale
import org.ghrobotics.robot.subsytems.SubsystemPreset
import org.ghrobotics.robot.subsytems.arm.ArmSubsystem
import org.ghrobotics.robot.subsytems.arm.ClosedLoopArmCommand
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem
import org.ghrobotics.robot.subsytems.elevator.ClosedLoopElevatorCommand
import org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem
import org.ghrobotics.robot.subsytems.intake.IntakeCommand
import org.ghrobotics.robot.subsytems.intake.IntakeSubsystem

class RoutineSwitchScaleFromCenter(
        startingPosition: Source<StartingPositions>,
        private val switchSide: Source<MatchData.OwnedSide>,
        private val scaleSide: Source<MatchData.OwnedSide>
) : AutoRoutine(startingPosition) {
    override fun createRoutine(): FalconCommand {
        val isLeftSwitch = switchSide.withEquals(MatchData.OwnedSide.LEFT)
        val switchMirrored = switchSide.withEquals(MatchData.OwnedSide.RIGHT)
        val scaleMirrored = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

        return sequential {
            +parallel {
                +DriveSubsystem.followTrajectory(isLeftSwitch, centerStartToLeftSwitch, centerStartToRightSwitch)
                +SubsystemPreset.SWITCH.command
                +sequential {
                    +DelayCommand(isLeftSwitch.map(centerStartToLeftSwitch, centerStartToRightSwitch)
                            .map { (it.lastState.t - 0.2).second })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, 0.5).withTimeout(200.millisecond)
                }
            }
            +parallel {
                +DriveSubsystem.followTrajectory(Trajectories.switchToCenter, switchMirrored)
                +sequential {
                    +DelayCommand(500.millisecond)
                    +SubsystemPreset.INTAKE.command
                }
            }
            +parallel {
                +DriveSubsystem.followTrajectory(Trajectories.centerToPyramid)
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(3.second)
            }
            +parallel {
                +DriveSubsystem.followTrajectory(pyramidToScale, scaleMirrored)
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition)
                +ClosedLoopArmCommand(Constants.kArmUpPosition)
                sequential {
                    +DelayCommand((pyramidToScale.lastState.t - 1.75).second)
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
package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.ConditionCommand
import frc.team5190.lib.commands.DelayCommand
import frc.team5190.lib.commands.sequential
import frc.team5190.lib.mathematics.units.NativeUnits
import frc.team5190.lib.utils.Source
import frc.team5190.lib.utils.map
import frc.team5190.lib.utils.observabletype.UpdatableObservableValue
import frc.team5190.robot.auto.StartingPositions
import frc.team5190.robot.auto.Trajectories
import frc.team5190.robot.subsytems.SubsystemPreset
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import frc.team5190.robot.subsytems.intake.IntakeCommand
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import openrio.powerup.MatchData
import java.util.concurrent.TimeUnit

class RoutineSwitchScaleFromCenter(startingPosition: Source<StartingPositions>,
                                   private val switchSide: Source<MatchData.OwnedSide>,
                                   private val scaleSide: Source<MatchData.OwnedSide>) : AutoRoutine(startingPosition) {
    override fun createRoutine(): Command {
        val switch = switchSide.withEquals(MatchData.OwnedSide.LEFT)
        val switchMirrored = switchSide.withEquals(MatchData.OwnedSide.RIGHT)
        val scaleMirrored = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

        val drop1stCube = FollowTrajectoryCommand(switch.map(Trajectories.centerStartToLeftSwitch, Trajectories.centerStartToRightSwitch))
        val toCenter = FollowTrajectoryCommand(Trajectories.switchToCenter, switchMirrored)
        val toPyramid = FollowTrajectoryCommand(Trajectories.centerToPyramid)
        val drop2ndCube = FollowTrajectoryCommand(Trajectories.pyramidToScale, scaleMirrored)

        return sequential {
            +parallel {
                +drop1stCube
                +SubsystemPreset.SWITCH.command
                +sequential {
                    +DelayCommand(((drop1stCube.trajectory.value.lastState.t - 0.2) * 1000).toLong(), TimeUnit.MILLISECONDS)
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, Source(0.5)).withTimeout(200, TimeUnit.MILLISECONDS)
                }
            }
            +parallel {
                +toCenter
                +sequential {
                    +DelayCommand(500, TimeUnit.MILLISECONDS)
                    +SubsystemPreset.INTAKE.command
                }
            }
            +parallel {
                +toPyramid
                +IntakeCommand(IntakeSubsystem.Direction.IN).withTimeout(3L, TimeUnit.SECONDS)
            }
            +parallel {
                +drop2ndCube
                +sequential {
                    +DelayCommand(((drop2ndCube.trajectory.value.lastState.t - 1.75) * 1000).toLong(), TimeUnit.MILLISECONDS)
                    +SubsystemPreset.BEHIND.command
                    +ConditionCommand(UpdatableObservableValue { ArmSubsystem.currentPosition > ArmSubsystem.kBehindPosition - NativeUnits(100) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, Source(0.35)).withTimeout(500, TimeUnit.MILLISECONDS)
                }
            }
        }
    }
}
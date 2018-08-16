package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.ConditionCommand
import frc.team5190.lib.commands.DelayCommand
import frc.team5190.lib.commands.condition
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.lib.utils.Source
import frc.team5190.lib.utils.State
import frc.team5190.lib.utils.map
import frc.team5190.lib.utils.withEquals
import frc.team5190.robot.auto.StartingPositions
import frc.team5190.robot.auto.Trajectories
import frc.team5190.robot.subsytems.SubsystemPreset
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

        val elevatorUp = drop2ndCube.addMarkerAt(Translation2d(11.5, 23.1).let { scaleMirrored.map(it.mirror, it) })
        val shoot1stCube = drop1stCube.addMarkerAt(
                Trajectories.kSwitchLeftAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-0.2, 0.0)))
                        .translation.let { switchMirrored.map(it.mirror, it) })

        val shoot2ndCube = drop2ndCube.addMarkerAt((Translation2d(22.3, 20.6)).let { scaleMirrored.map(it.mirror, it) })

        return parallel {
            sequential {
                +drop1stCube
                +toCenter
                +toPyramid
                +drop2ndCube
            }
            sequential {
                +DelayCommand(250L, TimeUnit.MILLISECONDS)
                +SubsystemPreset.SWITCH.command.withExit(condition(drop1stCube))
                +ConditionCommand(shoot1stCube.condition)
                +IntakeCommand(IntakeSubsystem.Direction.OUT).withTimeout(500L)
                +SubsystemPreset.INTAKE.command.withExit(condition(toCenter))
                +ConditionCommand(condition(toCenter))
                +IntakeCommand(IntakeSubsystem.Direction.IN).withExit(condition(toPyramid))
                +ConditionCommand(elevatorUp.condition)
                +SubsystemPreset.BEHIND.command.withExit(condition(drop2ndCube))
                +ConditionCommand(shoot2ndCube.condition)
                +IntakeCommand(IntakeSubsystem.Direction.OUT)
            }
        }
    }
}
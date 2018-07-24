/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.CommandGroup
import frc.team5190.lib.commands.ConditionCommand
import frc.team5190.lib.commands.condition
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.auto.Trajectories
import frc.team5190.robot.subsytems.SubsystemPreset
import frc.team5190.robot.subsytems.SubsystemPresetCommand
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import frc.team5190.robot.subsytems.intake.IntakeCommand
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import openrio.powerup.MatchData

class RoutineSwitchFromCenter(startingPosition: Autonomous.StartingPositions,
                              private val switchSide: MatchData.OwnedSide) : BaseRoutine(startingPosition) {
    override val routine: CommandGroup
        get() {
            val switch = if (switchSide == MatchData.OwnedSide.LEFT) {
                "Left"
            } else "Right"
            val mirrored = switchSide == MatchData.OwnedSide.RIGHT

            val drop1stCube = FollowTrajectoryCommand("Center Start to $switch Switch")
            val toCenter    = FollowTrajectoryCommand("Switch to Center", mirrored)
            val toPyramid   = FollowTrajectoryCommand("Center to Pyramid")
            val drop2ndCube = FollowTrajectoryCommand("Center to Switch", mirrored)

            val shoot1stCube = drop1stCube.addMarkerAt(
                    Trajectories.kSwitchLeftAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-0.2, 0.0)))
                            .translation.let { if (mirrored) it.mirror() else it })

            val shoot2ndCube = drop2ndCube.addMarkerAt(
                    Trajectories.kSwitchLeftAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-0.2, 0.0)))
                            .translation.let { if (mirrored) it.mirror() else it })

            return parallel {
                sequential {
                    +drop1stCube
                    +toCenter
                    +toPyramid
                    +FollowTrajectoryCommand("Pyramid to Center")
                    +drop2ndCube
                }
                sequential {
                    +SubsystemPresetCommand(SubsystemPreset.SWITCH, condition(drop1stCube))
                    +ConditionCommand(condition { drop1stCube.hasCrossedMarker(shoot1stCube) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT, timeout = 500L)
                    +SubsystemPresetCommand(SubsystemPreset.INTAKE, condition(toCenter))
                    +ConditionCommand(condition(toCenter))
                    +IntakeCommand(IntakeSubsystem.Direction.IN, exitCondition = condition(toPyramid))
                    +SubsystemPresetCommand(SubsystemPreset.SWITCH, condition(drop2ndCube))
                    +ConditionCommand(condition { drop2ndCube.hasCrossedMarker(shoot2ndCube) })
                    +IntakeCommand(IntakeSubsystem.Direction.OUT)
                }
            }
        }
}
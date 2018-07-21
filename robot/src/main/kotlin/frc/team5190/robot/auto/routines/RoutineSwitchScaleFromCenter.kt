/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.extensions.sequential
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import openrio.powerup.MatchData

class RoutineSwitchScaleFromCenter(startingPosition: Autonomous.StartingPositions,
                                   private val switchSide: MatchData.OwnedSide,
                                   private val scaleSide: MatchData.OwnedSide) : BaseRoutine(startingPosition) {
    override val routine: CommandGroup
        get() {
            val switch = if (switchSide == MatchData.OwnedSide.LEFT) {
                "Left"
            } else "Right"
            val switchMirrored = switchSide == MatchData.OwnedSide.RIGHT
            val scaleMirorred  = scaleSide == MatchData.OwnedSide.RIGHT

            val drop2ndCube  = FollowTrajectoryCommand(identifier = "Pyramid to Scale", pathMirrored = scaleMirorred)
            val shoot2ndCube = drop2ndCube.addMarkerAt(Translation2d(22.3, 20.6))

            return sequential {
                +FollowTrajectoryCommand(identifier = "Center Start to $switch Switch", pathMirrored = false)
                +FollowTrajectoryCommand(identifier = "Switch to Center", pathMirrored = switchMirrored)
                +FollowTrajectoryCommand(identifier = "Center to Pyramid")
                +drop2ndCube
            }
        }

}
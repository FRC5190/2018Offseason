package org.ghrobotics.robot.auto.routines

/* ktlint-disable no-wildcard-imports */
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.map
import org.ghrobotics.lib.utils.withEquals
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.Trajectories
import org.ghrobotics.robot.auto.Trajectories.centerStartToLeftSwitch
import org.ghrobotics.robot.auto.Trajectories.centerStartToRightSwitch
import org.ghrobotics.robot.auto.Trajectories.pyramidToScale
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

class RoutineSwitchScaleFromCenter(
    startingPosition: Source<StartingPositions>,
    private val switchSide: Source<MatchData.OwnedSide>,
    private val scaleSide: Source<MatchData.OwnedSide>
) : AutoRoutine(startingPosition) {
    override fun createRoutine(): FalconCommand {
        val isLeftSwitch = switchSide.withEquals(MatchData.OwnedSide.LEFT)
        val switchMirrored = switchSide.withEquals(MatchData.OwnedSide.RIGHT)
        val scaleMirrored = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

        val firstCubePath = isLeftSwitch.map(centerStartToLeftSwitch, centerStartToRightSwitch)

        return sequential {
            +DriveSubsystem.followTrajectory(firstCubePath)

            +DriveSubsystem.followTrajectory(Trajectories.switchToCenter, switchMirrored)

            +DriveSubsystem.followTrajectory(Trajectories.centerToPyramid)

            +DriveSubsystem.followTrajectory(pyramidToScale, scaleMirrored)
        }
    }
}
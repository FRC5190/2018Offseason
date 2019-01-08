package org.ghrobotics.robot.auto.routines

/* ktlint-disable no-wildcard-imports */
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.utils.map
import org.ghrobotics.lib.utils.withEquals
import org.ghrobotics.robot.auto.Autonomous.Config.scaleSide
import org.ghrobotics.robot.auto.Autonomous.Config.switchSide
import org.ghrobotics.robot.auto.Trajectories
import org.ghrobotics.robot.auto.Trajectories.centerStartToLeftSwitch
import org.ghrobotics.robot.auto.Trajectories.centerStartToRightSwitch
import org.ghrobotics.robot.auto.Trajectories.pyramidToScale
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

fun switchAndScaleRoutine() = autoRoutine {
    val isLeftSwitch = switchSide.withEquals(MatchData.OwnedSide.LEFT)
    val switchMirrored = switchSide.withEquals(MatchData.OwnedSide.RIGHT)
    val scaleMirrored = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

    val firstCubePath = isLeftSwitch.map(centerStartToLeftSwitch, centerStartToRightSwitch)

    +sequential {
        +DriveSubsystem.followTrajectory(firstCubePath)

        +DriveSubsystem.followTrajectory(Trajectories.switchToCenter, switchMirrored)

        +DriveSubsystem.followTrajectory(Trajectories.centerToPyramid)

        +DriveSubsystem.followTrajectory(pyramidToScale, scaleMirrored)
    }
}
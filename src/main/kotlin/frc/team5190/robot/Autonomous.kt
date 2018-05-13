package frc.team5190.robot

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.lib.util.Pathreader
import frc.team5190.lib.util.commandGroup
import kotlinx.coroutines.experimental.async
import openrio.powerup.MatchData

object Autonomous {

    // Switch side and scale side variables
    private var switchSide = MatchData.OwnedSide.UNKNOWN
    private var scaleSide = MatchData.OwnedSide.UNKNOWN

    // Starting position
    private var startingPosition = StartingPosition.CENTER

    // Contains folder in which paths are located
    private var folder = ""

    // Is FMS Data valid
    private val fmsDataValid
        get() = switchSide != MatchData.OwnedSide.UNKNOWN && scaleSide != MatchData.OwnedSide.UNKNOWN

    init {
        val startingPositionChooser = SendableChooser<StartingPosition>()
        StartingPosition.values().forEach { startingPositionChooser.addObject(it.name.toLowerCase().capitalize(), it) }

        // Poll for FMS Data
        async {
            while (!(Robot.INSTANCE.isAutonomous && Robot.INSTANCE.isEnabled && fmsDataValid && Pathreader.pathsGenerated)) {
                switchSide = MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR)
                scaleSide = MatchData.getOwnedSide(MatchData.GameFeature.SCALE)
                startingPosition = startingPositionChooser.selected
            }
            folder = if (startingPosition.name.first().toUpperCase() == scaleSide.name.first().toUpperCase()) "LS-LL" else "LS-RR"
            start()
        }
    }

    private fun start() {
        commandGroup {
            addSequential(FollowPathCommand(folder = "Test", file = "20 Feet", resetRobotPosition = true))
        }.start()
    }

}

enum class StartingPosition {
    LEFT, CENTER, RIGHT
}
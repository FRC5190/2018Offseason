package frc.team5190.robot

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.commandGroup
import frc.team5190.lib.util.Pathreader
import frc.team5190.robot.drive.CharacterizationCommand
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.robot.sensors.NavX
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import openrio.powerup.MatchData

object Autonomous {

    // Switch side and scale side variables
    private var switchSide = MatchData.OwnedSide.UNKNOWN
        @Synchronized get

    private var scaleSide = MatchData.OwnedSide.UNKNOWN
        @Synchronized get

    // Starting position
    private var startingPosition = StartingPosition.CENTER

    // Contains folder IN which paths are located
    private var folder = ""

    // Is FMS Data valid
    private val fmsDataValid
        get() = switchSide != MatchData.OwnedSide.UNKNOWN && scaleSide != MatchData.OwnedSide.UNKNOWN

    init {
        // Poll for FMS Data
        async {

            var autoCommand = commandGroup { }

            while (!(Robot.INSTANCE.isAutonomous && Robot.INSTANCE.isEnabled && fmsDataValid && Pathreader.pathsGenerated)) {
                if (StartingPosition.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase()) != startingPosition ||
                        MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) != switchSide ||
                        MatchData.getOwnedSide(MatchData.GameFeature.SCALE) != scaleSide) {

                    switchSide = MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR)
                    scaleSide = MatchData.getOwnedSide(MatchData.GameFeature.SCALE)
                    startingPosition = StartingPosition.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase())

                    autoCommand = getAutoCommand()
                    Robot.INSTANCE.isAutoReady = false

                    delay(100)
                }
                Robot.INSTANCE.isAutoReady = fmsDataValid && Pathreader.pathsGenerated
            }

            folder = if (startingPosition.name.first().toUpperCase() == scaleSide.name.first().toUpperCase()) "LS-LL" else "LS-RR"
            autoCommand.start()
        }
    }

    private fun getAutoCommand(): CommandGroup {
        NavX.reset()
        NavX.angleOffset = 0.0

        NetworkInterface.ntInstance.getEntry("Reset").setBoolean(true)

        return commandGroup { addSequential(CharacterizationCommand()) }
    }
}

enum class StartingPosition {
    LEFT, CENTER, RIGHT
}
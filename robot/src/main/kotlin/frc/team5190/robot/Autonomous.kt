package frc.team5190.robot

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.commandGroup
import frc.team5190.lib.kthx
import frc.team5190.lib.todo
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.robot.sensors.NavX
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import openrio.powerup.MatchData
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

object Autonomous {

    // Switch side and scale side variables
    private var switchSide = MatchData.OwnedSide.UNKNOWN
        @Synchronized get

    private var scaleSide = MatchData.OwnedSide.UNKNOWN
        @Synchronized get

    // Starting relativePos
    private var startingPosition = StartingPositions.CENTER

    // Contains folder IN which paths are located
    private var folder = ""

    // Is FMS Data valid
    private val fmsDataValid
        get() = switchSide != MatchData.OwnedSide.UNKNOWN && scaleSide != MatchData.OwnedSide.UNKNOWN

    // Random variables that make infix functions work
    private const val fivecube = ""
    private const val bye = ""

    init {
        // Poll for FMS Data
        launch {
            var sirpls = commandGroup { }

            while (!(Robot.INSTANCE.isAutonomous && Robot.INSTANCE.isEnabled && fmsDataValid && PathGenerator.pathsGenerated)) {
                if (StartingPositions.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase()) != startingPosition ||
                        MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) != switchSide ||
                        MatchData.getOwnedSide(MatchData.GameFeature.SCALE) != scaleSide) {

                    switchSide = MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR)
                    scaleSide = MatchData.getOwnedSide(MatchData.GameFeature.SCALE)
                    startingPosition = StartingPositions.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase())

                    folder = if (startingPosition.name.first().toUpperCase() == scaleSide.name.first().toUpperCase()) "LS-LL" else "LS-RR"

                    Localization.reset(position = startingPosition.relativePos)

                    sirpls = getAutoCommand()
                    Robot.INSTANCE.isAutoReady = false

                    delay(100)
                }
                Robot.INSTANCE.isAutoReady = fmsDataValid && PathGenerator.pathsGenerated == true
            }
            sirpls todo fivecube kthx bye
        }
    }

    private fun getAutoCommand(): CommandGroup {
        NavX.reset()
        NavX.angleOffset = 0.0

        NetworkInterface.ntInstance.getEntry("Reset").setBoolean(true)

        return commandGroup { addSequential(FollowPathCommand("Angled Test",
                robotReversed = false,
                resetRobotPosition = true,
                pathMirrored = startingPosition == StartingPositions.RIGHT)) }
    }
}

enum class StartingPositions(val relativePos: Vector2D) {
    LEFT(Vector2D(1.75, 23.5)), CENTER(Vector2D(1.75, 13.25)), RIGHT(Vector2D(1.75, 3.5))
}
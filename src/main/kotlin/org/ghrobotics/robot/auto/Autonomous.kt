package org.ghrobotics.robot.auto

/* ktlint-disable no-wildcard-imports */
/* ktlint-disable no-wildcard-imports */
import kotlinx.coroutines.GlobalScope
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.InstantRunnableCommand
import org.ghrobotics.lib.commands.S3ND
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.commands.stateCommandGroup
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.utils.*
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.Robot
import org.ghrobotics.robot.auto.routines.*
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

object Autonomous {

    object Config {
        val startingPosition = { NetworkInterface.startingPositionChooser.selected }
        val switchSide = { MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) }
        val scaleSide = { MatchData.getOwnedSide(MatchData.GameFeature.SCALE) }
        val switchAutoMode = { NetworkInterface.switchAutoChooser.selected }
        val scaleAutoMode = { NetworkInterface.scaleAutoChooser.selected }
        val autoMode = { NetworkInterface.autoModeChooser.selected }
    }

    val isSameSide = Config.startingPosition.withMerge(Config.scaleSide) { one, two -> one.isSameSide(two) }

    private var configValid = Config.switchSide.withMerge(Config.scaleSide) { one, two ->
        one != MatchData.OwnedSide.UNKNOWN && two != MatchData.OwnedSide.UNKNOWN
    }

    private val isReady = ({ FalconRobotBase.INSTANCE.run { isAutonomous && isEnabled } } and configValid)

    //
    private val JUST = sequential {
        +InstantRunnableCommand {
            println(Config.startingPosition())
            println(Config.scaleAutoMode())
        }
        +stateCommandGroup(Config.autoMode) {
            state(AutoMode.CHARACTERIZATION, characterizationRoutine())
            state(AutoMode.REAL) {
                stateCommandGroup(Config.startingPosition) {
                    state(StartingPositions.LEFT, StartingPositions.RIGHT) {
                        stateCommandGroup(Config.scaleAutoMode) {
                            state(ScaleAutoMode.THREECUBE, scaleRoutine())
                            state(ScaleAutoMode.BASELINE, baselineRoutine())
                        }
                    }
                    state(StartingPositions.CENTER) {
                        stateCommandGroup(Config.switchAutoMode) {
                            state(SwitchAutoMode.BASIC, switchRoutine())
                            state(SwitchAutoMode.ROBONAUTS, switchAndScaleRoutine())
                        }
                    }
                }
            }
        }
    }

    init {
        @Suppress("LocalVariableName")
        val IT = ""

        val startingPositionMonitor = Config.startingPosition.monitor
        val isReadyMonitor = isReady.monitor
        val modeMonitor = { Robot.currentMode }.monitor

        GlobalScope.launchFrequency {
            startingPositionMonitor.onChange { DriveSubsystem.localization.reset(it.pose) }
            isReadyMonitor.onChangeToTrue { JUST S3ND IT }
            modeMonitor.onChange { newValue ->
                if (newValue != FalconRobotBase.Mode.AUTONOMOUS) JUST.stop()
            }
        }
    }
}

enum class StartingPositions(
    val pose: Pose2d,
    private val matchSide: MatchData.OwnedSide
) {
    LEFT(Trajectories.kSideStart, MatchData.OwnedSide.LEFT),
    CENTER(Trajectories.kCenterStart, MatchData.OwnedSide.UNKNOWN),
    RIGHT(Trajectories.kSideStart.mirror, MatchData.OwnedSide.RIGHT);

    fun isSameSide(side: MatchData.OwnedSide) = matchSide == side
}

enum class SwitchAutoMode {
    BASIC, ROBONAUTS
}

enum class ScaleAutoMode {
    THREECUBE, BASELINE
}

enum class AutoMode {
    REAL, CHARACTERIZATION
}
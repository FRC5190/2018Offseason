package org.ghrobotics.robot.auto

/* ktlint-disable no-wildcard-imports */
/* ktlint-disable no-wildcard-imports */
import kotlinx.coroutines.GlobalScope
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.*
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.kilogram
import org.ghrobotics.lib.utils.*
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.auto.routines.*
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

object Autonomous {

    object Config {
        val startingPosition = { NetworkInterface.startingPositionChooser.selected }
        val switchSide = { MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) }
        val scaleSide = { MatchData.getOwnedSide(MatchData.GameFeature.SCALE) }
        val switchAutoMode = { NetworkInterface.switchAutoChooser.selected }
        val scaleAutoMode = { NetworkInterface.scaleAutoChooser.selected }
    }

    val isSameSide = Config.startingPosition.withMerge(Config.scaleSide) { one, two -> one.isSameSide(two) }

    private var configValid = Config.switchSide.withMerge(Config.scaleSide) { one, two ->
        one != MatchData.OwnedSide.UNKNOWN && two != MatchData.OwnedSide.UNKNOWN
    }

    private val shouldPoll = !({ FalconRobotBase.INSTANCE.run { isAutonomous && isEnabled } } and configValid)

//
    private val JUST = sequential {
        +InstantRunnableCommand {
            println(Config.startingPosition())
            println(Config.scaleAutoMode())
        }
        +stateCommandGroup(Config.startingPosition) {
            state(StartingPositions.LEFT, StartingPositions.RIGHT) {
                stateCommandGroup(Config.scaleAutoMode) {
                    state(
                        ScaleAutoMode.THREECUBE,
                        RoutineScaleFromSide(Config.startingPosition, Config.scaleSide)
                    )
                    // Baseline same side
                    state(
                        ScaleAutoMode.BASELINE,
                        RoutineBaseline(Config.startingPosition)
                    )
                }
            }
            state(StartingPositions.CENTER) {
                stateCommandGroup(Config.switchAutoMode) {
                    // Center 2 cube
                    state(
                        SwitchAutoMode.BASIC,
                        RoutineSwitchFromCenter(Config.startingPosition, Config.switchSide)
                    )
                    // Center 1 cube switch and 1 cube scale
                    state(
                        SwitchAutoMode.ROBONAUTS,
                        RoutineSwitchScaleFromCenter(
                            Config.startingPosition,
                            Config.switchSide,
                            Config.scaleSide
                        )
                    )
                }
            }
        }
    }


    init {
        @Suppress("LocalVariableName")
        val IT = ""

        val startingPositionMonitor = Config.startingPosition.monitor
        val shouldPollMonitor = shouldPoll.monitor
        GlobalScope.launchFrequency {
            startingPositionMonitor.onChange { DriveSubsystem.localization.reset(it.pose) }
            shouldPollMonitor.onChangeToFalse { println("sending it"); JUST S3ND IT }
        }
        FalconRobotBase.INSTANCE.onLeave(FalconRobotBase.Mode.AUTONOMOUS) { JUST.stop() }
    }

    private fun <T> StateCommandGroupBuilder<T>.state(state: T, routine: AutoRoutine) = state(state, routine.create())
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
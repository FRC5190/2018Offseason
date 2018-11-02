package org.ghrobotics.robot.auto

import kotlinx.coroutines.GlobalScope
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.S3ND
import org.ghrobotics.lib.commands.StateCommandGroupBuilder
import org.ghrobotics.lib.commands.stateCommandGroup
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
/* ktlint-disable no-wildcard-imports */
import org.ghrobotics.lib.utils.*
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.auto.routines.AutoRoutine
import org.ghrobotics.robot.auto.routines.RoutineScaleFromSide
import org.ghrobotics.robot.auto.routines.RoutineSwitchFromCenter
import org.ghrobotics.robot.auto.routines.RoutineSwitchScaleFromCenter
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

object Autonomous {

    object Config {
        val startingPosition = { NetworkInterface.startingPositionChooser.selected }
        val switchSide = { MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) }
        val scaleSide = { MatchData.getOwnedSide(MatchData.GameFeature.SCALE) }
        val switchAutoMode = { NetworkInterface.switchAutoChooser.selected }
        val nearScaleAutoMode = { NetworkInterface.nearScaleAutoChooser.selected }
        val farScaleAutoMode = { NetworkInterface.farScaleAutoChooser.selected }
    }

    val isSameSide = Config.startingPosition.withMerge(Config.scaleSide) { one, two -> !one.isSameSide(two) }

    private var configValid = Config.switchSide.withMerge(Config.scaleSide) { one, two ->
        one != MatchData.OwnedSide.UNKNOWN && two != MatchData.OwnedSide.UNKNOWN
    }

    private val shouldPoll = !({ FalconRobotBase.INSTANCE.run { isAutonomous && isEnabled } } and configValid)

    // Autonomous Master Group
    private val JUST = stateCommandGroup(Config.startingPosition) {
        state(StartingPositions.LEFT, StartingPositions.RIGHT) {
            stateCommandGroup(isSameSide) {
                state(true) {
                    stateCommandGroup(Config.nearScaleAutoMode) {
                        // Three cube same side
                        state(
                                ScaleAutoMode.THREECUBE,
                                RoutineScaleFromSide(Config.startingPosition, Config.scaleSide)
                        )
                        // Baseline same side
                        state(
                                ScaleAutoMode.BASELINE,
                                RoutineScaleFromSide(Config.startingPosition, Config.scaleSide)
                        )
                    }
                }
                state(false) {
                    stateCommandGroup(Config.farScaleAutoMode) {
                        // 2.5 cube cross
                        state(
                                ScaleAutoMode.THREECUBE,
                                RoutineScaleFromSide(Config.startingPosition, Config.scaleSide)
                        )
                        // Baseline cross
                        state(
                                ScaleAutoMode.BASELINE,
                                RoutineScaleFromSide(Config.startingPosition, Config.scaleSide)
                        )
                    }
                }
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

    init {
        @Suppress("LocalVariableName")
        val IT = ""

        val startingPositionMonitor = Config.startingPosition.monitor
        val shouldPollMonitor = shouldPoll.monitor
        GlobalScope.launchFrequency {
            startingPositionMonitor.onChange { DriveSubsystem.localization.reset(it.pose) }
            shouldPollMonitor.onChangeToFalse { JUST S3ND IT }
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
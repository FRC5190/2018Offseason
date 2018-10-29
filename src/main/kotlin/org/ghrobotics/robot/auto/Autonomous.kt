package org.ghrobotics.robot.auto

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.S3ND
import org.ghrobotics.lib.commands.StateCommandGroupBuilder
import org.ghrobotics.lib.commands.stateCommandGroup
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.observabletype.and
import org.ghrobotics.lib.utils.observabletype.map
import org.ghrobotics.lib.utils.observabletype.not
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.lib.utils.withMerge
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.auto.routines.AutoRoutine
import org.ghrobotics.robot.auto.routines.RoutineScaleFromSide
import org.ghrobotics.robot.auto.routines.RoutineSwitchFromCenter
import org.ghrobotics.robot.auto.routines.RoutineSwitchScaleFromCenter
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

object Autonomous {

    object Config {
        val startingPosition = GlobalScope.updatableValue { NetworkInterface.startingPositionChooser.selected }
        val switchSide = GlobalScope.updatableValue { MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) }
        val scaleSide = GlobalScope.updatableValue { MatchData.getOwnedSide(MatchData.GameFeature.SCALE) }
        val switchAutoMode: Source<SwitchAutoMode> = { NetworkInterface.switchAutoChooser.selected }
        val nearScaleAutoMode: Source<ScaleAutoMode> = { NetworkInterface.nearScaleAutoChooser.selected }
        val farScaleAutoMode: Source<ScaleAutoMode> = { NetworkInterface.farScaleAutoChooser.selected }
    }

    val isSameSide = Config.startingPosition.asSource()
            .withMerge(Config.scaleSide.asSource()) { one, two -> !one.isSameSide(two) }

    private var configValid =
            Config.switchSide.map { it != MatchData.OwnedSide.UNKNOWN } and Config.scaleSide.map { it != MatchData.OwnedSide.UNKNOWN }
    private val shouldPoll =
            !(GlobalScope.updatableValue(5) { FalconRobotBase.INSTANCE.run { isAutonomous && isEnabled } } and configValid)

    // Autonomous Master Group
    private val JUST = stateCommandGroup(Config.startingPosition.asSource()) {
        state(StartingPositions.LEFT, StartingPositions.RIGHT) {
            stateCommandGroup(isSameSide) {
                state(true) {
                    stateCommandGroup(Config.nearScaleAutoMode) {
                        // Three cube same side
                        state(
                                ScaleAutoMode.THREECUBE,
                                RoutineScaleFromSide(Config.startingPosition.asSource(), Config.scaleSide.asSource())
                        )
                        // Baseline same side
                        state(
                                ScaleAutoMode.BASELINE,
                                RoutineScaleFromSide(Config.startingPosition.asSource(), Config.scaleSide.asSource())
                        )
                    }
                }
                state(false) {
                    stateCommandGroup(Config.farScaleAutoMode) {
                        // 2.5 cube cross
                        state(
                                ScaleAutoMode.THREECUBE,
                                RoutineScaleFromSide(Config.startingPosition.asSource(), Config.scaleSide.asSource())
                        )
                        // Baseline cross
                        state(
                                ScaleAutoMode.BASELINE,
                                RoutineScaleFromSide(Config.startingPosition.asSource(), Config.scaleSide.asSource())
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
                        RoutineSwitchFromCenter(Config.startingPosition.asSource(), Config.switchSide.asSource())
                )
                // Center 1 cube switch and 1 cube scale
                state(
                        SwitchAutoMode.ROBONAUTS,
                        RoutineSwitchScaleFromCenter(
                                Config.startingPosition.asSource(),
                                Config.switchSide.asSource(),
                                Config.scaleSide.asSource()
                        )
                )
            }
        }
    }

    init {
        @Suppress("LocalVariableName")
        val IT = ""
        shouldPoll.invokeOnChange {
            if (it) return@invokeOnChange
            JUST S3ND IT
        }
        FalconRobotBase.INSTANCE.modeStateMachine.onLeave(listOf(FalconRobotBase.Mode.AUTONOMOUS)) {
            JUST.stop()
        }
        Config.startingPosition.invokeOnChange { runBlocking { DriveSubsystem.localization.reset(it.pose); } }
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
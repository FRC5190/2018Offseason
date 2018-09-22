package org.ghrobotics.robot.auto

import kotlinx.coroutines.experimental.runBlocking
import openrio.powerup.MatchData
import org.ghrobotics.lib.commands.S3ND
import org.ghrobotics.lib.commands.StateCommandGroupBuilder
import org.ghrobotics.lib.commands.stateCommandGroup
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue
import org.ghrobotics.lib.utils.observabletype.and
import org.ghrobotics.lib.utils.observabletype.not
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.Localization
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.auto.routines.AutoRoutine
import org.ghrobotics.robot.auto.routines.RoutineScaleFromSide
import org.ghrobotics.robot.auto.routines.RoutineSwitchFromCenter
import org.ghrobotics.robot.auto.routines.RoutineSwitchScaleFromCenter
import org.ghrobotics.robot.sensors.AHRS

object Autonomous {

    object Config {
        val startingPosition = UpdatableObservableValue { NetworkInterface.startingPositionChooser.selected }
        val switchSide = UpdatableObservableValue { MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) }
        val scaleSide = UpdatableObservableValue { MatchData.getOwnedSide(MatchData.GameFeature.SCALE) }
        val switchAutoMode = Source { NetworkInterface.switchAutoChooser.selected }
        val nearScaleAutoMode = Source { NetworkInterface.nearScaleAutoChooser.selected }
        val farScaleAutoMode = Source { NetworkInterface.farScaleAutoChooser.selected }
    }

    private val farScale = Config.startingPosition.asSource().withMerge(Config.scaleSide.asSource()) { one, two -> !one.name.first().equals(two.name.first(), true) }

    private var configValid = Config.switchSide.map { it != MatchData.OwnedSide.UNKNOWN } and Config.scaleSide.map { it != MatchData.OwnedSide.UNKNOWN }
    private val shouldPoll = !(UpdatableObservableValue(5) { FalconRobotBase.INSTANCE.run { isAutonomous && isEnabled } } and configValid)


    // Autonomous Master Group
    private val JUST = stateCommandGroup(Config.startingPosition.asSource()) {
        state(StartingPositions.LEFT, StartingPositions.RIGHT) {
            stateCommandGroup(farScale) {
                state(true) {
                    stateCommandGroup(Config.farScaleAutoMode) {
                        state(ScaleAutoMode.THREECUBE, RoutineScaleFromSide(Config.startingPosition.asSource(), Config.scaleSide.asSource()))
                        state(ScaleAutoMode.BASELINE, RoutineScaleFromSide(Config.startingPosition.asSource(), Config.scaleSide.asSource()))
                    }
                }
                state(false) {
                    stateCommandGroup(Config.nearScaleAutoMode) {
                        state(ScaleAutoMode.THREECUBE, RoutineScaleFromSide(Config.startingPosition.asSource(), Config.scaleSide.asSource()))
                        state(ScaleAutoMode.BASELINE, RoutineScaleFromSide(Config.startingPosition.asSource(), Config.scaleSide.asSource()))
                    }
                }
            }
        }
        println("a4")
        state(StartingPositions.CENTER) {
            stateCommandGroup(Config.switchAutoMode) {
                state(SwitchAutoMode.BASIC, RoutineSwitchFromCenter(Config.startingPosition.asSource(), Config.switchSide.asSource()))
                state(SwitchAutoMode.ROBONAUTS, RoutineSwitchScaleFromCenter(Config.startingPosition.asSource(), Config.switchSide.asSource(), Config.scaleSide.asSource()))
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
        Config.startingPosition.invokeOnChange { runBlocking { Localization.reset(it.pose); } }
    }


    private fun <T> StateCommandGroupBuilder<T>.state(state: T, routine: AutoRoutine) = state(state, routine.create())
}

enum class StartingPositions(val pose: Pose2d) {
    LEFT(Trajectories.kSideStart),
    CENTER(Trajectories.kCenterStart),
    RIGHT(Trajectories.kSideStart.mirror)
}

enum class SwitchAutoMode {
    BASIC, ROBONAUTS
}

enum class ScaleAutoMode {
    THREECUBE, BASELINE
}
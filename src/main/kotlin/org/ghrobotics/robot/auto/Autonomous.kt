package org.ghrobotics.robot.auto

import org.ghrobotics.lib.commands.S3ND
import org.ghrobotics.lib.commands.StateCommandGroupBuilder
import org.ghrobotics.lib.commands.stateCommandGroup
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.observabletype.ObservableValue
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue
import org.ghrobotics.lib.utils.observabletype.and
import org.ghrobotics.lib.utils.observabletype.not
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.NetworkInterface
import org.ghrobotics.robot.auto.routines.*
import openrio.powerup.MatchData

object Autonomous {

    object Config {
        val startingPosition = Source { NetworkInterface.startingPositionChooser.selected }
        val switchSide = autoConfigListener { MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) }
        val scaleSide = autoConfigListener { MatchData.getOwnedSide(MatchData.GameFeature.SCALE) }
        val switchAutoMode = Source { NetworkInterface.switchAutoChooser.selected }
        val nearScaleAutoMode = Source { NetworkInterface.nearScaleAutoChooser.selected }
        val farScaleAutoMode = Source { NetworkInterface.farScaleAutoChooser.selected }
    }

    private val farScale = Config.startingPosition.withMerge(Config.scaleSide.asSource()) { one, two -> !one.name.first().equals(two.name.first(), true) }

    private var configValid = Config.switchSide.map { it != MatchData.OwnedSide.UNKNOWN } and Config.scaleSide.map { it != MatchData.OwnedSide.UNKNOWN }
    private val shouldPoll = !(UpdatableObservableValue(5) { FalconRobotBase.INSTANCE.run { isAutonomous && isEnabled } } and configValid)


    // Autonomous Master Group
    private val JUST = stateCommandGroup(Config.startingPosition) {
        state(StartingPositions.LEFT, StartingPositions.RIGHT) {
            stateCommandGroup(farScale) {
                state(true) {
                    stateCommandGroup(Config.farScaleAutoMode) {
                        state(ScaleAutoMode.THREECUBE, RoutineScaleFromSide(Config.startingPosition, Config.scaleSide.asSource()))
                        state(ScaleAutoMode.BASELINE, RoutineBaseline(Config.startingPosition))
                    }
                }
                state(false) {
                    stateCommandGroup(Config.nearScaleAutoMode) {
                        state(ScaleAutoMode.THREECUBE, RoutineScaleFromSide(Config.startingPosition, Config.scaleSide.asSource()))
                        state(ScaleAutoMode.BASELINE, RoutineBaseline(Config.startingPosition))
                    }
                }
            }
        }
        println("a4")
        state(StartingPositions.CENTER) {
            stateCommandGroup(Config.switchAutoMode) {
                state(SwitchAutoMode.BASIC, RoutineSwitchFromCenter(Config.startingPosition, Config.switchSide.asSource()))
                state(SwitchAutoMode.ROBONAUTS, RoutineSwitchScaleFromCenter(Config.startingPosition, Config.switchSide.asSource(), Config.scaleSide.asSource()))
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
    }


    private fun <T> StateCommandGroupBuilder<T>.state(state: T, routine: AutoRoutine) = state(state, routine.create())
    private fun <T> autoConfigListener(block: () -> T): ObservableValue<T> = UpdatableObservableValue(block = block)
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
package frc.team5190.robot.auto

import frc.team5190.lib.extensions.S3ND
import frc.team5190.lib.extensions.StateCommandGroupBuilder
import frc.team5190.lib.extensions.stateCommandGroup
import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.utils.Source
import frc.team5190.lib.utils.launchFrequency
import frc.team5190.lib.utils.statefulvalue.StatefulValue
import frc.team5190.lib.utils.statefulvalue.StatefulValueImpl
import frc.team5190.lib.utils.statefulvalue.and
import frc.team5190.lib.utils.statefulvalue.not
import frc.team5190.lib.wrappers.FalconRobotBase
import frc.team5190.robot.NetworkInterface
import frc.team5190.robot.auto.routines.*
import kotlinx.coroutines.experimental.newSingleThreadContext
import openrio.powerup.MatchData

object Autonomous {

    private val autoContext = newSingleThreadContext("Autonomous")

    object Config {
        val startingPosition = Source { StartingPositions.valueOf(NetworkInterface.startingPosition.toUpperCase()) }
        val switchSide = autoConfigListener { MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) }
        val scaleSide = autoConfigListener { MatchData.getOwnedSide(MatchData.GameFeature.SCALE) }
        val switchAutoMode = Source { SwitchAutoMode.valueOf(NetworkInterface.switchAutoMode.toUpperCase()) }
        val nearScaleAutoMode = Source { ScaleAutoMode.valueOf(NetworkInterface.nearScaleAutoMode.toUpperCase()) }
        val farScaleAutoMode = Source { ScaleAutoMode.valueOf(NetworkInterface.farScaleAutoMode.toUpperCase()) }
    }

    private val farScale = Config.startingPosition.withMerge(Config.scaleSide.asSource()) { one, two -> !one.name.first().equals(two.name.first(), true) }

    private var configValid = Config.switchSide.withProcessing { it != MatchData.OwnedSide.UNKNOWN } and Config.scaleSide.withProcessing { it != MatchData.OwnedSide.UNKNOWN }
    private val shouldPoll = !(StatefulValue(5) { FalconRobotBase.INSTANCE.run { isAutonomous && isEnabled } } and configValid)

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
                    println("a5")
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
                println("a3")
            }
        }
    }

    init {
        @Suppress("LocalVariableName")
        val IT = ""
        shouldPoll.invokeOnChange {
            println("a1")
            if (it) return@invokeOnChange
            JUST S3ND IT
        }
        FalconRobotBase.INSTANCE.modeStateMachine.onLeave(listOf(FalconRobotBase.Mode.AUTONOMOUS)) {
            JUST.stop()
        }
        println("a2")
    }


    private fun <T> StateCommandGroupBuilder<T>.state(state: T, routine: AutoRoutine) = state(state, routine.create())

    private fun <T> autoConfigListener(block: () -> T): StatefulValue<T> = AutoState(block = block)

    private class AutoState<T>(private val block: () -> T) : StatefulValueImpl<T>(block()) {
        init {
            launchFrequency(20, autoContext) {
                changeValue(block())
            }
        }
    }

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
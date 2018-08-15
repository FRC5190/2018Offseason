package frc.team5190.robot.auto

import frc.team5190.lib.commands.and
import frc.team5190.lib.extensions.S3ND
import frc.team5190.lib.extensions.StateCommandGroupBuilder
import frc.team5190.lib.extensions.stateCommandGroup
import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.utils.*
import frc.team5190.lib.wrappers.FalconRobotBase
import frc.team5190.robot.NetworkInterface
import frc.team5190.robot.auto.routines.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.newSingleThreadContext
import openrio.powerup.MatchData

object Autonomous {

    private val autoContext = newSingleThreadContext("Autonomous")

    object Config {
        val startingPosition = variableSource { StartingPositions.valueOf(NetworkInterface.startingPosition.toUpperCase()) }
        val switchSide = autoConfigListener { MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) }
        val scaleSide = autoConfigListener { MatchData.getOwnedSide(MatchData.GameFeature.SCALE) }
        val switchAutoMode = variableSource { SwitchAutoMode.valueOf(NetworkInterface.switchAutoMode.toUpperCase()) }
        val nearScaleAutoMode = variableSource { ScaleAutoMode.valueOf(NetworkInterface.nearScaleAutoMode.toUpperCase()) }
        val farScaleAutoMode = variableSource { ScaleAutoMode.valueOf(NetworkInterface.farScaleAutoMode.toUpperCase()) }
    }

    private val farScale = mergeSource(Config.startingPosition, Config.scaleSide) { one, two -> !one.name.first().equals(two.name.first(), true) }

    private var configValid = comparisionState(Config.switchSide, Config.scaleSide) { one, two -> one != MatchData.OwnedSide.UNKNOWN && two != MatchData.OwnedSide.UNKNOWN }
    private val shouldPoll = !(updatableState(5) { FalconRobotBase.INSTANCE.run { isAutonomous && isEnabled } } and configValid)

    // Autonomous Master Group

    private val JUST = stateCommandGroup(Config.startingPosition) {
        state(StartingPositions.LEFT, StartingPositions.RIGHT) {
            stateCommandGroup(farScale) {
                state(true) {
                    stateCommandGroup(Config.farScaleAutoMode) {
                        state(ScaleAutoMode.THREECUBE, RoutineScaleFromSide(Config.startingPosition, Config.scaleSide))
                        state(ScaleAutoMode.BASELINE, RoutineBaseline(Config.startingPosition))
                    }
                }
                state(false) {
                    stateCommandGroup(Config.nearScaleAutoMode) {
                        state(ScaleAutoMode.THREECUBE, RoutineScaleFromSide(Config.startingPosition, Config.scaleSide))
                        state(ScaleAutoMode.BASELINE, RoutineBaseline(Config.startingPosition))
                    }
                }
            }
        }
        state(StartingPositions.CENTER) {
            stateCommandGroup(Config.switchAutoMode) {
                state(SwitchAutoMode.BASIC, RoutineSwitchFromCenter(Config.startingPosition, Config.switchSide))
                state(SwitchAutoMode.ROBONAUTS, RoutineSwitchScaleFromCenter(Config.startingPosition, Config.switchSide, Config.scaleSide))
            }
        }
    }

    init {
        val IT = ""
        shouldPoll.invokeWhenFalse {
            JUST S3ND IT
        }
    }


    private fun <T> StateCommandGroupBuilder<T>.state(state: T, routine: AutoRoutine) = state(state, routine.create())

    private fun <T> autoConfigListener(block: () -> T): StateImpl<T> = AutoState(block = block)

    private class AutoState<T>(private val block: () -> T) : StateImpl<T>(block()) {
        private var pollJob: Job? = null
        override fun initWhenUsed() {
            shouldPoll.invokeWhenTrue {
                pollJob = launchFrequency(20, autoContext) {
                    internalValue = block()
                }
            }
            shouldPoll.invokeWhenFalse {
                pollJob?.cancel()
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
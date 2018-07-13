/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto

import edu.wpi.first.wpilibj.DriverStation
import frc.team5190.lib.extensions.S3ND
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.robot.NetworkInterface
import frc.team5190.robot.Robot
import frc.team5190.robot.auto.routines.RoutineBaseline
import frc.team5190.robot.auto.routines.RoutineScaleFromSide
import frc.team5190.robot.auto.routines.RoutineSwitchFromCenter
import frc.team5190.robot.auto.routines.RoutineSwitchScaleFromCenter
import kotlinx.coroutines.experimental.launch
import openrio.powerup.MatchData
import openrio.powerup.MatchData.GameFeature.SCALE
import openrio.powerup.MatchData.GameFeature.SWITCH_NEAR
import openrio.powerup.MatchData.getOwnedSide

@Suppress("LocalVariableName")
object Autonomous {

    private var switchSide = MatchData.OwnedSide.UNKNOWN
    private var scaleSide = MatchData.OwnedSide.UNKNOWN

    private var startingPosition = StartingPositions.CENTER
    private var switchAutoMode = SwitchAutoMode.SWITCH
    private var nearScaleAutoMode = ScaleAutoMode.SCALE
    private var farScaleAutoMode = ScaleAutoMode.SCALE


    private val farScale
        get() = startingPosition.name.first().toUpperCase() != scaleSide.name.first().toUpperCase()

    private val fmsDataValid
        get() = switchSide != MatchData.OwnedSide.UNKNOWN && scaleSide != MatchData.OwnedSide.UNKNOWN

    private val networkStartingPosition
        get() = StartingPositions.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase())

    private val networkSwitchAutoMode
        get() = SwitchAutoMode.valueOf(NetworkInterface.switchAutoMode.getString("Switch").toUpperCase())

    private val networkNearScaleAutoMode
        get() = ScaleAutoMode.valueOf(NetworkInterface.nearScaleAutoMode.getString("Scale").toUpperCase())

    private val networkFarScaleAutoMode
        get() = ScaleAutoMode.valueOf(NetworkInterface.farScaleAutoMode.getString("Scale").toUpperCase())


    private val continuePolling
        get() = (Robot.INSTANCE.isAutonomous && Robot.INSTANCE.isEnabled && fmsDataValid).not()

    private val dataChanged
        get() = networkStartingPosition != startingPosition ||
                getOwnedSide(SWITCH_NEAR) != switchSide ||
                getOwnedSide(SCALE) != scaleSide ||
                networkSwitchAutoMode != switchAutoMode ||
                networkNearScaleAutoMode != nearScaleAutoMode ||
                networkFarScaleAutoMode != farScaleAutoMode


    init {
        // Poll for FMS Data
        launch {
            var JUST = RoutineBaseline(startingPosition).routine
            val IT = ""

            while (continuePolling) {
                if (dataChanged) {

                    switchSide = getOwnedSide(SWITCH_NEAR)
                    scaleSide = getOwnedSide(SCALE)
                    startingPosition = networkStartingPosition


                    JUST = when (startingPosition) {
                        StartingPositions.LEFT, StartingPositions.RIGHT -> {
                            if (farScale) {
                                when (farScaleAutoMode) {
                                    Autonomous.ScaleAutoMode.SCALE -> RoutineScaleFromSide(startingPosition, scaleSide).routine
                                    Autonomous.ScaleAutoMode.BASELINE -> RoutineBaseline(startingPosition).routine
                                }
                            } else {
                                when (nearScaleAutoMode) {
                                    Autonomous.ScaleAutoMode.SCALE -> RoutineScaleFromSide(startingPosition, scaleSide).routine
                                    Autonomous.ScaleAutoMode.BASELINE -> RoutineBaseline(startingPosition).routine
                                }
                            }
                        }
                        StartingPositions.CENTER -> {
                            when (switchAutoMode) {
                                Autonomous.SwitchAutoMode.SWITCH -> RoutineSwitchFromCenter(startingPosition, switchSide).routine
                                Autonomous.SwitchAutoMode.ROBONAUTS -> RoutineSwitchScaleFromCenter(startingPosition, switchSide, scaleSide).routine
                            }
                        }
                    }
                }
            }
            println("[Autonomous] Game Data Received from FMS --> ${DriverStation.getInstance().gameSpecificMessage}")
            JUST S3ND IT
        }
    }

    enum class StartingPositions(val pose: Pose2d) {
        LEFT(Trajectories.kSideStart),
        CENTER(Trajectories.kCenterStart),
        RIGHT(Trajectories.kSideStart.mirror())
    }

    enum class SwitchAutoMode {
        SWITCH, ROBONAUTS
    }

    enum class ScaleAutoMode {
        SCALE, BASELINE
    }
}
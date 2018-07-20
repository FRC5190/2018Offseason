/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto

import edu.wpi.first.wpilibj.DriverStation
import frc.team5190.lib.extensions.S3ND
import frc.team5190.lib.math.geometry.Pose2d
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
    var scaleSide = MatchData.OwnedSide.UNKNOWN

    private var startingPosition = StartingPositions.CENTER
    private var switchAutoMode = SwitchAutoMode.BASIC
    private var nearScaleAutoMode = ScaleAutoMode.THREECUBE
    private var farScaleAutoMode = ScaleAutoMode.THREECUBE


    private val farScale
        get() = startingPosition.name.first().toUpperCase() != scaleSide.name.first().toUpperCase()

    private val fmsDataValid
        get() = switchSide != MatchData.OwnedSide.UNKNOWN && scaleSide != MatchData.OwnedSide.UNKNOWN

    private val networkStartingPosition
        get() = StartingPositions.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase())

    private val networkSwitchAutoMode
        get() = SwitchAutoMode.valueOf(NetworkInterface.switchAutoMode.getString("Basic").toUpperCase())

    private val networkNearScaleAutoMode
        get() = ScaleAutoMode.valueOf(NetworkInterface.nearScaleAutoMode.getString("Baseline").toUpperCase())

    private val networkFarScaleAutoMode
        get() = ScaleAutoMode.valueOf(NetworkInterface.farScaleAutoMode.getString("Baseline").toUpperCase())


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

                    switchAutoMode = networkSwitchAutoMode
                    nearScaleAutoMode = networkNearScaleAutoMode
                    farScaleAutoMode = networkFarScaleAutoMode


                    JUST = when (startingPosition) {
                        StartingPositions.LEFT, StartingPositions.RIGHT -> {
                            if (farScale) {
                                when (farScaleAutoMode) {
                                    Autonomous.ScaleAutoMode.THREECUBE -> RoutineScaleFromSide(startingPosition, scaleSide).routine
                                    Autonomous.ScaleAutoMode.BASELINE -> RoutineBaseline(startingPosition).routine
                                }
                            } else {
                                when (nearScaleAutoMode) {
                                    Autonomous.ScaleAutoMode.THREECUBE -> RoutineScaleFromSide(startingPosition, scaleSide).routine
                                    Autonomous.ScaleAutoMode.BASELINE -> RoutineBaseline(startingPosition).routine
                                }
                            }
                        }
                        StartingPositions.CENTER -> {
                            when (switchAutoMode) {
                                Autonomous.SwitchAutoMode.BASIC -> RoutineSwitchFromCenter(startingPosition, switchSide).routine
                                Autonomous.SwitchAutoMode.ROBONAUTS -> RoutineSwitchScaleFromCenter(startingPosition, switchSide, scaleSide).routine
                            }
                        }
                    }

                    println("\n" +
                            "[Autonomous]\n" +
                            "Game Data:            ${DriverStation.getInstance().gameSpecificMessage}\n" +
                            "Starting Position:    ${startingPosition.name}\n" +
                            "Near Scale Auto Mode: $nearScaleAutoMode\n" +
                            "Far Scale Auto Mode:  $farScaleAutoMode\n" +
                            "Switch Auto Mode:     $switchAutoMode")
                }
            }
            JUST S3ND IT
        }
    }

    enum class StartingPositions(val pose: Pose2d) {
        LEFT(Trajectories.kSideStart),
        CENTER(Trajectories.kCenterStart),
        RIGHT(Trajectories.kSideStart.mirror())
    }

    enum class SwitchAutoMode {
        BASIC, ROBONAUTS
    }

    enum class ScaleAutoMode {
        THREECUBE, BASELINE
    }
}
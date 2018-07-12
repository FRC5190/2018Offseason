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


    private val fmsDataValid
        get() = switchSide != MatchData.OwnedSide.UNKNOWN && scaleSide != MatchData.OwnedSide.UNKNOWN

    private val networkStartingPosition
        get() = StartingPositions.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase())

    private val continuePolling
        get() = (Robot.INSTANCE.isAutonomous && Robot.INSTANCE.isEnabled && fmsDataValid).not()

    private val dataChanged
        get() = networkStartingPosition != startingPosition ||
                getOwnedSide(SWITCH_NEAR) != switchSide ||
                getOwnedSide(SCALE) != scaleSide


    init {
        // Poll for FMS Data
        launch {

            var JUST = RoutineBaseline().routine
            val IT = ""

            while (continuePolling) {
                if (dataChanged) {

                    switchSide       = getOwnedSide(SWITCH_NEAR)
                    scaleSide        = getOwnedSide(SCALE)
                    startingPosition = networkStartingPosition


                    JUST = when (startingPosition) {
                        StartingPositions.LEFT   -> RoutineScaleFromSide(startingPosition, scaleSide).routine
                        StartingPositions.RIGHT  -> RoutineScaleFromSide(startingPosition, scaleSide).routine
                        StartingPositions.CENTER -> RoutineSwitchFromCenter(switchSide).routine
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
}
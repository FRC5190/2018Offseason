package frc.team5190.robot.auto

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.extensions.S3ND
import frc.team5190.lib.extensions.sequential
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.robot.NetworkInterface
import frc.team5190.robot.Robot
import frc.team5190.robot.sensors.NavX
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
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

    private var farScale = false

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

    private val mirroredStart
        get() = startingPosition == StartingPositions.RIGHT

    private val autoCommand: CommandGroup
        get() {
            NavX.reset()
            NavX.angleOffset = startingPosition.pose.rotation.degrees

            NetworkInterface.ntInstance.getEntry("Reset").setBoolean(true)

            return sequential {
                FollowTrajectoryCommand(if (farScale) "Left Start to Far Scale" else "Left Start to Near Scale", mirroredStart)
                FollowTrajectoryCommand("Near Scale to Cube 1", scaleSide == MatchData.OwnedSide.RIGHT)
                FollowTrajectoryCommand("Cube 1 to Near Scale", scaleSide == MatchData.OwnedSide.RIGHT)
                FollowTrajectoryCommand("Near Scale to Cube 2", scaleSide == MatchData.OwnedSide.RIGHT)
                FollowTrajectoryCommand("Cube 2 to Near Scale", scaleSide == MatchData.OwnedSide.RIGHT)
                FollowTrajectoryCommand("Near Scale to Cube 3", scaleSide == MatchData.OwnedSide.RIGHT)
                FollowTrajectoryCommand("Cube 3 to Near Scale", scaleSide == MatchData.OwnedSide.RIGHT)
            }
        }


    init {
        // Poll for FMS Data
        launch {

            var JUST = sequential { }
            val IT = ""

            while (continuePolling) {
                if (dataChanged) {
                    switchSide = getOwnedSide(SWITCH_NEAR)
                    scaleSide =  getOwnedSide(SCALE)
                    startingPosition = networkStartingPosition

                    farScale = startingPosition.name.first().toUpperCase() != scaleSide.name.first().toUpperCase()

                    Localization.reset(startingPosition.pose)

                    JUST = autoCommand
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
}
package frc.team5190.robot

import edu.wpi.first.networktables.NetworkTableInstance
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.robot.sensors.Pigeon
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

object NetworkInterface {
    init {
        launch {
            val ntInstance = NetworkTableInstance.getDefault().getTable("Live Dashboard")

            val startingPosition = ntInstance.getEntry("Starting Position")
            val sameSideAuto = ntInstance.getEntry("Same Side Auto")
            val crossAuto = ntInstance.getEntry("Cross Auto")

            val robotX = ntInstance.getEntry("Robot X")
            val robotY = ntInstance.getEntry("Robot Y")
            val robotHdg = ntInstance.getEntry("Robot Heading")

            val pathX = ntInstance.getEntry("Path X")
            val pathY = ntInstance.getEntry("Path Y")
            val pathHdg = ntInstance.getEntry("Path Heading")

            val lookaheadX = ntInstance.getEntry("Lookahead X")
            val lookaheadY = ntInstance.getEntry("Lookahead Y")

            val driveLeftEncoder = ntInstance.getEntry("Drive Left Encoder")
            val driveLeftPercent = ntInstance.getEntry("Drive Left Pct")
            val driveLeftAmps = ntInstance.getEntry("Drive Left Amps")

            val driveRightEncoder = ntInstance.getEntry("Drive Right Encoder")
            val driveRightPercent = ntInstance.getEntry("Drive Right Pct")
            val driveRightAmps = ntInstance.getEntry("Drive Right Amps")

            val elevatorEncoder = ntInstance.getEntry("Elevator Encoder")
            val elevatorPercent = ntInstance.getEntry("Elevator Pct")
            val elevatorAmps = ntInstance.getEntry("Elevator Amps")

            val armEncoder = ntInstance.getEntry("Arm Encoder")
            val armPercent = ntInstance.getEntry("Arm Pct")
            val armAmps = ntInstance.getEntry("Arm Amps")

            val climbEncoder = ntInstance.getEntry("Climb Encoder")
            val climbPercent = ntInstance.getEntry("Climb Pct")
            val climbAmps = ntInstance.getEntry("Climb Amps")

            val isClimbing = ntInstance.getEntry("Is Climbing")

            val isConnected = ntInstance.getEntry("Is Connected")
            val isEnabled = ntInstance.getEntry("Is Enabled")

            val gameData = ntInstance.getEntry("Game Data")

            while (true) {
                robotX.setDouble(Localization.robotPosition.x)
                robotY.setDouble(Localization.robotPosition.y)
                robotHdg.setDouble(Math.toRadians(Pigeon.correctedAngle))

                pathX.setDouble(FollowPathCommand.pathX)
                pathY.setDouble(FollowPathCommand.pathY)
                pathHdg.setDouble(FollowPathCommand.pathHdg)

                lookaheadX.setDouble(FollowPathCommand.lookaheadX)
                lookaheadY.setDouble(FollowPathCommand.lookaheadY)

                delay(10, TimeUnit.MILLISECONDS)
            }
        }
    }
}
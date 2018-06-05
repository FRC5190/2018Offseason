package frc.team5190.robot

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import frc.team5190.robot.arm.ArmSubsystem
import frc.team5190.robot.climb.ClimbSubsystem
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.robot.elevator.ElevatorSubsystem
import frc.team5190.robot.sensors.Pigeon
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.apache.commons.math3.ml.neuralnet.Network
import java.util.concurrent.TimeUnit

@Suppress("HasPlatformType")
object NetworkInterface {

    val ntInstance = NetworkTableInstance.getDefault().getTable("Live Dashboard")

    val startingPosition = ntInstance.getEntry("Starting Position")
    val sameSideAuto = ntInstance.getEntry("Same Side Auto")
    val crossAuto = ntInstance.getEntry("Cross Auto")

    private val robotX = ntInstance.getEntry("Robot X")
    private val robotY = ntInstance.getEntry("Robot Y")
    private val robotHdg = ntInstance.getEntry("Robot Heading")

    private val pathX = ntInstance.getEntry("Path X")
    private val pathY = ntInstance.getEntry("Path Y")
    private val pathHdg = ntInstance.getEntry("Path Heading")

    private val lookaheadX = ntInstance.getEntry("Lookahead X")
    private val lookaheadY = ntInstance.getEntry("Lookahead Y")

    private val driveLeftEncoder = ntInstance.getEntry("Drive Left Encoder")
    private val driveLeftPercent = ntInstance.getEntry("Drive Left Pct")
    private val driveLeftAmps = ntInstance.getEntry("Drive Left Amps")

    private val driveRightEncoder = ntInstance.getEntry("Drive Right Encoder")
    private val driveRightPercent = ntInstance.getEntry("Drive Right Pct")
    private val driveRightAmps = ntInstance.getEntry("Drive Right Amps")

    private val elevatorEncoder = ntInstance.getEntry("Elevator Encoder")
    private val elevatorPercent = ntInstance.getEntry("Elevator Pct")
    private val elevatorAmps = ntInstance.getEntry("Elevator Amps")

    private val armEncoder = ntInstance.getEntry("Arm Encoder")
    private val armPercent = ntInstance.getEntry("Arm Pct")
    private val armAmps = ntInstance.getEntry("Arm Amps")

    private val climbEncoder = ntInstance.getEntry("Climb Encoder")
    private val climbPercent = ntInstance.getEntry("Climb Pct")
    private val climbAmps = ntInstance.getEntry("Climb Amps")

    private val isClimbing = ntInstance.getEntry("Is Climbing")
    private val isEnabled = ntInstance.getEntry("Is Enabled")

    private val gameData = ntInstance.getEntry("Game Data")


    init {
        launch {
            while (true) {
                robotX.setDouble(Localization.robotPosition.x)
                robotY.setDouble(Localization.robotPosition.y)
                robotHdg.setDouble(Math.toRadians(Pigeon.correctedAngle))

                pathX.setDouble(FollowPathCommand.pathX)
                pathY.setDouble(FollowPathCommand.pathY)
                pathHdg.setDouble(FollowPathCommand.pathHdg)

                lookaheadX.setDouble(FollowPathCommand.lookaheadX)
                lookaheadY.setDouble(FollowPathCommand.lookaheadY)

                driveLeftEncoder.setDouble(DriveSubsystem.leftPosition.STU.value.toDouble())
                driveLeftPercent.setDouble(DriveSubsystem.leftPercent)
                driveLeftAmps.setDouble(DriveSubsystem.leftAmperage)

                driveRightEncoder.setDouble(DriveSubsystem.rightPosition.STU.value.toDouble())
                driveRightPercent.setDouble(DriveSubsystem.rightPercent)
                driveRightAmps.setDouble(DriveSubsystem.rightAmperage)

                elevatorEncoder.setDouble(ElevatorSubsystem.currentPosition.STU.value.toDouble())
                elevatorPercent.setDouble(ElevatorSubsystem.percent)
                elevatorAmps.setDouble(ElevatorSubsystem.amperage)

                armEncoder.setDouble(ArmSubsystem.currentPosition.STU.value.toDouble())
                armPercent.setDouble(ArmSubsystem.percent)
                armAmps.setDouble(ArmSubsystem.amperage)

                climbEncoder.setDouble(ClimbSubsystem.currentPosition.STU.value.toDouble())
                climbPercent.setDouble(ClimbSubsystem.percent)
                climbAmps.setDouble(ClimbSubsystem.amperage)

                isClimbing.setBoolean(Robot.INSTANCE.isClimbing)
                isEnabled.setBoolean(Robot.INSTANCE.isEnabled)

                gameData.setString(DriverStation.getInstance().gameSpecificMessage ?: "null")

                delay(10, TimeUnit.MILLISECONDS)
            }
        }
    }
}
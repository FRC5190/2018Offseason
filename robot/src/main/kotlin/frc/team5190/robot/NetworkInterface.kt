/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier
import frc.team5190.robot.sensors.NavX
import frc.team5190.robot.subsytems.drive.DriveSubsystem
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand

@Suppress("HasPlatformType")
object NetworkInterface  {

    val kInstance = NetworkTableInstance.getDefault().getTable("Live Dashboard")

    val startingPosition = kInstance.getEntry("Starting Position")

    private val robotX = kInstance.getEntry("Robot X")
    private val robotY = kInstance.getEntry("Robot Y")

    private val robotHdg = kInstance.getEntry("Robot Heading")

    private val pathX = kInstance.getEntry("Path X")
    private val pathY = kInstance.getEntry("Path Y")
    private val pathHdg = kInstance.getEntry("Path Heading")

    private val lookaheadX = kInstance.getEntry("Lookahead X")
    private val lookaheadY = kInstance.getEntry("Lookahead Y")

    private val driveLeftEncoder = kInstance.getEntry("Drive Left Encoder")
    private val driveLeftPercent = kInstance.getEntry("Drive Left Pct")
    private val driveLeftAmps = kInstance.getEntry("Drive Left Amps")

    private val driveRightEncoder = kInstance.getEntry("Drive Right Encoder")
    private val driveRightPercent = kInstance.getEntry("Drive Right Pct")
    private val driveRightAmps = kInstance.getEntry("Drive Right Amps")

    private val isEnabled = kInstance.getEntry("Is Enabled")
    private val gameData = kInstance.getEntry("Game Data")

    private val notifier: Notifier


    init {
        notifier = Notifier {
            robotX.setDouble(Localization.robotPosition.translation.x)
            robotY.setDouble(Localization.robotPosition.translation.y)

            robotHdg.setDouble(Math.toRadians(NavX.correctedAngle))

            pathX.setDouble(FollowTrajectoryCommand.pathX)
            pathY.setDouble(FollowTrajectoryCommand.pathY)
            pathHdg.setDouble(FollowTrajectoryCommand.pathHdg)

            lookaheadX.setDouble(FollowTrajectoryCommand.lookaheadX)
            lookaheadY.setDouble(FollowTrajectoryCommand.lookaheadY)

            driveLeftEncoder.setDouble(DriveSubsystem.leftPosition.STU.toDouble())
            driveLeftPercent.setDouble(DriveSubsystem.leftPercent * 100)
            driveLeftAmps.setDouble(DriveSubsystem.leftAmperage)

            driveRightEncoder.setDouble(DriveSubsystem.rightPosition.STU.toDouble())
            driveRightPercent.setDouble(DriveSubsystem.rightPercent * 100)
            driveRightAmps.setDouble(DriveSubsystem.rightAmperage)

            isEnabled.setString(if (Robot.INSTANCE.isEnabled) "Enabled" else "Disabled")
            gameData.setString(DriverStation.getInstance().gameSpecificMessage ?: "null")
        }

        notifier.startPeriodic(0.02)
    }
}
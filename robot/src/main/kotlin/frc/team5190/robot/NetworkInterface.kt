/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier
import frc.team5190.robot.subsytems.drive.DriveSubsystem
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand

@Suppress("HasPlatformType")
object NetworkInterface  {

    val INSTANCE = NetworkTableInstance.getDefault().getTable("Live Dashboard")

    val startingPosition = INSTANCE.getEntry("Starting Position")

    val switchAutoMode = INSTANCE.getEntry("Switch Auto Mode")

    private val robotX = INSTANCE.getEntry("Robot X")
    private val robotY = INSTANCE.getEntry("Robot Y")

    private val robotHdg = INSTANCE.getEntry("Robot Heading")

    private val pathX = INSTANCE.getEntry("Path X")
    private val pathY = INSTANCE.getEntry("Path Y")
    private val pathHdg = INSTANCE.getEntry("Path Heading")

    private val lookaheadX = INSTANCE.getEntry("Lookahead X")
    private val lookaheadY = INSTANCE.getEntry("Lookahead Y")

    private val driveLeftEncoder = INSTANCE.getEntry("Drive Left Encoder")
    private val driveLeftPercent = INSTANCE.getEntry("Drive Left Pct")
    private val driveLeftAmps = INSTANCE.getEntry("Drive Left Amps")

    private val driveRightEncoder = INSTANCE.getEntry("Drive Right Encoder")
    private val driveRightPercent = INSTANCE.getEntry("Drive Right Pct")
    private val driveRightAmps = INSTANCE.getEntry("Drive Right Amps")

    private val isEnabled = INSTANCE.getEntry("Is Enabled")
    private val gameData = INSTANCE.getEntry("Game Data")

    private val notifier: Notifier


    init {
        notifier = Notifier {
            robotX.setDouble(Localization.robotPosition.translation.x)
            robotY.setDouble(Localization.robotPosition.translation.y)

            robotHdg.setDouble(Localization.robotPosition.rotation.degrees)

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
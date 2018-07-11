/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier
import frc.team5190.robot.auto.Localization
import frc.team5190.robot.subsytems.drive.DriveSubsystem
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import frc.team5190.robot.sensors.NavX

@Suppress("HasPlatformType")
object NetworkInterface {

    val ntInstance = NetworkTableInstance.getDefault().getTable("Live Dashboard")

    val startingPosition = ntInstance.getEntry("Starting Position")

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

    private val isEnabled = ntInstance.getEntry("Is Enabled")
    private val gameData = ntInstance.getEntry("Game Data")

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
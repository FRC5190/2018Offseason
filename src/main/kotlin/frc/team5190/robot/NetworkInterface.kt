/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier
import frc.team5190.lib.wrappers.FalconRobotBase
import frc.team5190.lib.wrappers.networktables.FalconNetworkTable
import frc.team5190.lib.wrappers.networktables.get
import frc.team5190.robot.subsytems.drive.DriveSubsystem
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import frc.team5190.lib.wrappers.networktables.*

@Suppress("HasPlatformType")
object NetworkInterface {

    val INSTANCE = FalconNetworkTable.getTable("Live Dashboard")

    val startingPosition by INSTANCE["Starting Position"].delegate("Center")

    val switchAutoMode by INSTANCE["Switch Auto Mode"].delegate("Basic")
    val nearScaleAutoMode by INSTANCE["Near Scale Auto Mode"].delegate("Baseline")
    val farScaleAutoMode by INSTANCE["Far Scale Auto Mode"].delegate("Baseline")

    private val robotX = INSTANCE["Robot X"]
    private val robotY = INSTANCE["Robot Y"]

    private val robotHdg = INSTANCE["Robot Heading"]

    private val pathX = INSTANCE["Path X"]
    private val pathY = INSTANCE["Path Y"]
    private val pathHdg = INSTANCE["Path Heading"]

    private val lookaheadX = INSTANCE["Lookahead X"]
    private val lookaheadY = INSTANCE["Lookahead Y"]

    private val driveLeftEncoder = INSTANCE["Drive Left Encoder"]
    private val driveLeftPercent = INSTANCE["Drive Left Pct"]
    private val driveLeftAmps = INSTANCE["Drive Left Amps"]

    private val driveRightEncoder = INSTANCE["Drive Right Encoder"]
    private val driveRightPercent = INSTANCE["Drive Right Pct"]
    private val driveRightAmps = INSTANCE["Drive Right Amps"]

    private val isEnabled = INSTANCE["Is Enabled"]
    private val gameData = INSTANCE["Game Data"]

    private val notifier: Notifier

    init {
        notifier = Notifier {
            robotX.setDouble(Localization.robotPosition.translation.x)
            robotY.setDouble(Localization.robotPosition.translation.y)

            robotHdg.setDouble(Localization.robotPosition.rotation.radians)

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

            isEnabled.setString(if (FalconRobotBase.INSTANCE.isEnabled) "Enabled" else "Disabled")
            gameData.setString(DriverStation.getInstance().gameSpecificMessage ?: "null")
        }

        notifier.startPeriodic(0.02)
    }
}
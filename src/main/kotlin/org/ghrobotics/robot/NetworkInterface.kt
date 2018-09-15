/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.lib.wrappers.networktables.FalconNetworkTable
import org.ghrobotics.lib.wrappers.networktables.get
import org.ghrobotics.robot.auto.ScaleAutoMode
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.SwitchAutoMode
import org.ghrobotics.robot.subsytems.drive.FollowTrajectoryCommand

@Suppress("HasPlatformType")
object NetworkInterface {

    val INSTANCE = FalconNetworkTable.getTable("Live Dashboard")

    val startingPositionChooser = SendableChooser<StartingPositions>()

    val nearScaleAutoChooser = SendableChooser<ScaleAutoMode>()
    val farScaleAutoChooser = SendableChooser<ScaleAutoMode>()
    val switchAutoChooser = SendableChooser<SwitchAutoMode>()

    private val robotX = INSTANCE["Robot X"]
    private val robotY = INSTANCE["Robot Y"]

    private val robotHdg = INSTANCE["Robot Heading"]

    private val pathX = INSTANCE["Path X"]
    private val pathY = INSTANCE["Path Y"]
    private val pathHdg = INSTANCE["Path Heading"]

    private val lookaheadX = INSTANCE["Lookahead X"]
    private val lookaheadY = INSTANCE["Lookahead Y"]

    private val isEnabled = INSTANCE["Is Enabled"]
    private val gameData = INSTANCE["Game Data"]

    private val notifier: Notifier

    init {
        StartingPositions.values().forEach { startingPositionChooser.addDefault(it.name.toLowerCase().capitalize(), it) }

        ScaleAutoMode.values().forEach {
            nearScaleAutoChooser.addDefault(it.name.toLowerCase().capitalize(), it)
            farScaleAutoChooser.addDefault(it.name.toLowerCase().capitalize(), it)
        }

        SwitchAutoMode.values().forEach { switchAutoChooser.addDefault(it.name.toLowerCase().capitalize(), it) }

        SmartDashboard.putData("Starting Position", startingPositionChooser)
        SmartDashboard.putData("Near Scale Auto Mode", farScaleAutoChooser)
        SmartDashboard.putData("Far Scale Auto Mode", farScaleAutoChooser)
        SmartDashboard.putData("Switch Auto Mode", switchAutoChooser)

        notifier = Notifier {
            val x = Localization.robotPosition.translation.x
            val y = Localization.robotPosition.translation.y
            val a = Localization.robotPosition.rotation.radians

            robotX.setDouble(x)
            robotY.setDouble(y)
            robotHdg.setDouble(a)

            pathX.setDouble(FollowTrajectoryCommand.pathX)
            pathY.setDouble(FollowTrajectoryCommand.pathY)
            pathHdg.setDouble(FollowTrajectoryCommand.pathHdg)

            lookaheadX.setDouble(FollowTrajectoryCommand.lookaheadX)
            lookaheadY.setDouble(FollowTrajectoryCommand.lookaheadY)

            isEnabled.setString(if (FalconRobotBase.INSTANCE.isEnabled) "Enabled" else "Disabled")
            gameData.setString(DriverStation.getInstance().gameSpecificMessage ?: "null")

            SmartDashboard.putNumber("Robot X", x)
            SmartDashboard.putNumber("Robot Y", y)
            SmartDashboard.putNumber("Robot Angle", a)
        }

        notifier.startPeriodic(0.02)
    }
}
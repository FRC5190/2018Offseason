/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.GlobalScope
import org.ghrobotics.lib.mathematics.twodim.control.TrajectoryFollower
import org.ghrobotics.lib.utils.launchFrequency
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.lib.wrappers.networktables.FalconNetworkTable
import org.ghrobotics.lib.wrappers.networktables.get
import org.ghrobotics.robot.auto.AutoMode
import org.ghrobotics.robot.auto.ScaleAutoMode
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.SwitchAutoMode
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

@Suppress("HasPlatformType")
object NetworkInterface {

    val INSTANCE = FalconNetworkTable.getTable("Live Dashboard")

    val startingPositionChooser = SendableChooser<StartingPositions>()

    val scaleAutoChooser = SendableChooser<ScaleAutoMode>()
    val switchAutoChooser = SendableChooser<SwitchAutoMode>()

    val autoModeChooser = SendableChooser<AutoMode>()

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

    init {
        StartingPositions.values()
            .forEach { startingPositionChooser.addDefault(it.name.toLowerCase().capitalize(), it) }

        ScaleAutoMode.values().forEach {
            scaleAutoChooser.addDefault(it.name.toLowerCase().capitalize(), it)
        }

        AutoMode.values().forEach {
            autoModeChooser.addDefault(it.name.toLowerCase().capitalize(), it)
        }

        SwitchAutoMode.values().forEach { switchAutoChooser.addDefault(it.name.toLowerCase().capitalize(), it) }

        SmartDashboard.putData("Starting Position", startingPositionChooser)
        SmartDashboard.putData("Scale Auto Mode", scaleAutoChooser)
        SmartDashboard.putData("Switch Auto Mode", switchAutoChooser)
        SmartDashboard.putData("Auto mode", autoModeChooser)

        GlobalScope.launchFrequency(50) {
            val robotPosition = DriveSubsystem.localization.robotPosition

            val x = robotPosition.translation.x.feet
            val y = robotPosition.translation.y.feet
            val a = robotPosition.rotation.radian

            robotX.setDouble(x)
            robotY.setDouble(y)
            robotHdg.setDouble(a)

            val trajectoryFollower: TrajectoryFollower = DriveSubsystem.trajectoryFollower

            pathX.setDouble(trajectoryFollower.referencePose.translation.x.feet)
            pathY.setDouble(trajectoryFollower.referencePose.translation.y.feet)
            pathHdg.setDouble(trajectoryFollower.referencePose.rotation.degree)
/*
            if(trajectoryFollower is PurePursuitController) {
                lookaheadX.setDouble(trajectoryFollower.lookaheadX.feet.asDouble)
                lookaheadY.setDouble(trajectoryFollower.lookaheadY.feet.asDouble)
            }*/

            isEnabled.setString(if (FalconRobotBase.INSTANCE.isEnabled) "Enabled" else "Disabled")
            gameData.setString(DriverStation.getInstance().gameSpecificMessage ?: "null")

            SmartDashboard.putNumber("Robot X", x)
            SmartDashboard.putNumber("Robot Y", y)
            SmartDashboard.putNumber("Robot Angle", robotPosition.rotation.degree)

            SmartDashboard.putNumber("Left Encoder Position", DriveSubsystem.leftMaster.sensorPosition.meter)
            SmartDashboard.putNumber("Right Encoder Position", DriveSubsystem.rightMaster.sensorPosition.meter)
        }
    }
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot

import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import kotlinx.coroutines.GlobalScope
import org.ghrobotics.lib.utils.launchFrequency
import org.ghrobotics.robot.auto.AutoMode
import org.ghrobotics.robot.auto.ScaleAutoMode
import org.ghrobotics.robot.auto.StartingPositions
import org.ghrobotics.robot.auto.SwitchAutoMode
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

@Suppress("HasPlatformType")
object NetworkInterface {

    private val mainShuffleboardDisplay: ShuffleboardTab = Shuffleboard.getTab("Main Display")

    private val leftPositionEntry: NetworkTableEntry = mainShuffleboardDisplay.add("Left Encoder", 0.0).entry
    private val rightPositionEntry: NetworkTableEntry = mainShuffleboardDisplay.add("Right Encoder", 0.0).entry

    val startingPositionChooser = SendableChooser<StartingPositions>()

    val scaleAutoChooser = SendableChooser<ScaleAutoMode>()
    val switchAutoChooser = SendableChooser<SwitchAutoMode>()

    val autoModeChooser = SendableChooser<AutoMode>()

    init {
        StartingPositions.values()
            .forEach { startingPositionChooser.addOption(it.name.toLowerCase().capitalize(), it) }

        ScaleAutoMode.values().forEach {
            scaleAutoChooser.addOption(it.name.toLowerCase().capitalize(), it)
        }

        AutoMode.values().forEach {
            autoModeChooser.addOption(it.name.toLowerCase().capitalize(), it)
        }

        SwitchAutoMode.values().forEach { switchAutoChooser.setDefaultOption(it.name.toLowerCase().capitalize(), it) }

        mainShuffleboardDisplay.add("Starting Position", startingPositionChooser)
        mainShuffleboardDisplay.add("Scale Auto Mode", scaleAutoChooser)
        mainShuffleboardDisplay.add("Switch Auto Mode", switchAutoChooser)
        mainShuffleboardDisplay.add("Auto mode", autoModeChooser)

        GlobalScope.launchFrequency(50) {
            leftPositionEntry.setDouble(DriveSubsystem.leftMotor.getSelectedSensorPosition(0).toDouble())
            rightPositionEntry.setDouble(DriveSubsystem.rightMotor.getSelectedSensorPosition(0).toDouble())
        }
    }
}
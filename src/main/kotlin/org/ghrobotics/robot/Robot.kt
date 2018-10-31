/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot

import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.auto.Autonomous
import org.ghrobotics.robot.sensors.AHRS
import org.ghrobotics.robot.sensors.AHRSSensorType
import org.ghrobotics.robot.sensors.Lidar
import org.ghrobotics.robot.sensors.ahrsSensorType
import org.ghrobotics.robot.subsytems.arm.ArmSubsystem
import org.ghrobotics.robot.subsytems.climber.ClimberSubsystem
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem
import org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem
import org.ghrobotics.robot.subsytems.intake.IntakeSubsystem
import org.ghrobotics.robot.subsytems.led.LEDSubsystem

class Robot : FalconRobotBase() {

    // Initialize all systems.
    override suspend fun initialize() {
        ahrsSensorType = AHRSSensorType.Pigeon

        +Controls.mainXbox

        +DriveSubsystem
        +ArmSubsystem
        +ElevatorSubsystem
        +ClimberSubsystem
        +IntakeSubsystem
        +LEDSubsystem

        println(1)
        NetworkInterface

        Autonomous
        println(2)
        AHRS
        println(3)
        Lidar
        println(4)
    }
}
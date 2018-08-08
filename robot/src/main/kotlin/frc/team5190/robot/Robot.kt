/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import frc.team5190.lib.wrappers.FalconRobotBase
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.sensors.AHRS
import frc.team5190.robot.sensors.AHRSSensorType
import frc.team5190.robot.sensors.Lidar
import frc.team5190.robot.sensors.ahrsSensorType
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.drive.DriveSubsystem
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.intake.IntakeSubsystem

class Robot : FalconRobotBase() {

    // Can't make entire class an object, so INSTANCE is initialized in a companion object.
    companion object {
        lateinit var INSTANCE: Robot
            private set
    }

    // Initialize instance.
    init {
        ahrsSensorType = AHRSSensorType.Pigeon

        INSTANCE = this
    }

    // Initialize all systems.
    override suspend fun initialize() {
        +Controls.mainXbox

        +DriveSubsystem
        +ArmSubsystem
        +ElevatorSubsystem
        +IntakeSubsystem

        Localization
        NetworkInterface
        Autonomous
        AHRS
        Lidar

    }

}
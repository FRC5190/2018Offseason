package frc.team5190.robot

import edu.wpi.first.wpilibj.IterativeRobot
import frc.team5190.lib.Pathreader
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.elevator.ElevatorSubsystem
import frc.team5190.robot.sensors.Canifier
import frc.team5190.robot.sensors.LEDs
import frc.team5190.robot.sensors.Pigeon

class Robot : IterativeRobot() {
    companion object {
        lateinit var INSTANCE: Robot
    }

    init {
        INSTANCE = this
    }

    override fun robotInit() {
        Localization
        Pathreader
        Autonomous
        Canifier
        Pigeon
        LEDs

        DriveSubsystem
        ElevatorSubsystem
    }

    override fun robotPeriodic() {
        Pigeon.update()
    }
}
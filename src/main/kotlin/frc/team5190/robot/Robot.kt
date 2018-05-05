package frc.team5190.robot

import edu.wpi.first.wpilibj.IterativeRobot
import frc.team5190.robot.auto.FollowPathCommand
import frc.team5190.robot.auto.Pathreader
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.sensors.Canifier
import frc.team5190.robot.sensors.LEDs
import frc.team5190.robot.sensors.Pigeon

class Robot : IterativeRobot() {

    override fun robotInit() {
        Localization
        Pathreader
        Canifier
        LEDs

        DriveSubsystem
    }

    override fun autonomousInit() {
        Pigeon.reset()
        Pigeon.angleOffset = 0.0
        FollowPathCommand(folder = "Test", file = "20 Feet", resetRobotPosition = true).start()
    }
}
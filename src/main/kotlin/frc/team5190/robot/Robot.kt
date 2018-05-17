package frc.team5190.robot

import edu.wpi.first.wpilibj.IterativeRobot
import edu.wpi.first.wpilibj.command.Scheduler
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.team5190.lib.Pathreader
import frc.team5190.robot.drive.DrivePathCommand
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
        SmartDashboard.putNumber("Robot X", Localization.robotPosition.x)
        SmartDashboard.putNumber("Robot Y", Localization.robotPosition.y)
        SmartDashboard.putNumber("Gyro", Pigeon.correctedAngle)

        Scheduler.getInstance().run()
    }

    override fun autonomousInit() {
        DrivePathCommand(folder = "LS-LL", file = "Cross", pathMirrored = true, resetRobotPosition = true).start()
    }
}
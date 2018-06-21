package frc.team5190.robot

import edu.wpi.first.wpilibj.IterativeRobot
import edu.wpi.first.wpilibj.command.Scheduler
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.sensors.NavX

class Robot : IterativeRobot() {

    companion object {
        lateinit var INSTANCE: Robot
    }

    init {
        INSTANCE = this
    }

    override fun robotInit() {
        PathGenerator
        Localization
        NetworkInterface
        Autonomous
        NavX

        DriveSubsystem
    }

    override fun robotPeriodic() {
        Scheduler.getInstance().run()
    }

    override fun disabledInit() {}
    override fun disabledPeriodic() {}

    override fun autonomousInit() {}
    override fun autonomousPeriodic() {}

    override fun teleopInit() {}
    override fun teleopPeriodic() {}
}
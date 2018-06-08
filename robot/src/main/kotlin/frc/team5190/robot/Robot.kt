package frc.team5190.robot

import edu.wpi.first.wpilibj.IterativeRobot
import edu.wpi.first.wpilibj.command.Scheduler
import frc.team5190.lib.util.Pathreader
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.sensors.NavX

class Robot : IterativeRobot() {

    var isAutoReady = false

    companion object {
        lateinit var INSTANCE: Robot
    }

    init {
        INSTANCE = this
    }

    override fun robotInit() {
        Localization
        NetworkInterface
        Pathreader
        Autonomous
        NavX

        DriveSubsystem
    }

    override fun robotPeriodic() {
        Scheduler.getInstance().run()
    }
}
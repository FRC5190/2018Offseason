package frc.team5190.robot

import edu.wpi.first.wpilibj.IterativeRobot
import edu.wpi.first.wpilibj.command.Scheduler
import frc.team5190.lib.util.PathReader
import frc.team5190.robot.arm.ArmSubsystem
import frc.team5190.robot.climb.ClimbSubsystem
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.elevator.ElevatorSubsystem
import frc.team5190.robot.intake.IntakeSubsystem
import frc.team5190.robot.sensors.Canifier
import frc.team5190.robot.sensors.LEDs
import frc.team5190.robot.sensors.Pigeon

class Robot : IterativeRobot() {

    // Global Robot Variables
    var isClimbing = false
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
        PathReader
        Autonomous
        Canifier
        Pigeon
        LEDs

        DriveSubsystem
        ElevatorSubsystem
        IntakeSubsystem
        ClimbSubsystem
        ArmSubsystem
    }

    override fun robotPeriodic() {
        Pigeon.update()
        Scheduler.getInstance().run()
    }
}
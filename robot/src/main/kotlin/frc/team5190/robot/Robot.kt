/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.wpilibj.IterativeRobot
import edu.wpi.first.wpilibj.command.Scheduler
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.sensors.NavX
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.climb.ClimbSubsystem
import frc.team5190.robot.subsytems.drive.DriveSubsystem
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.intake.IntakeSubsystem

class Robot : IterativeRobot() {

    // Can't make entire class an object, so INSTANCE is initialized in a companion object.
    companion object {
        lateinit var INSTANCE: Robot
    }

    // Initialize instance.
    init {
        INSTANCE = this
    }

    // Initialize all systems.
    override fun robotInit() {
        Localization
        NetworkInterface
        Autonomous
        NavX

        DriveSubsystem
        ArmSubsystem
        ElevatorSubsystem
        IntakeSubsystem
        ClimbSubsystem
    }

    // Run scheduler for command based processes.
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
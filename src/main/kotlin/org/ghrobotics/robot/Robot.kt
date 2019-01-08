/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot

import edu.wpi.first.wpilibj.RobotBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.auto.Autonomous
import org.ghrobotics.robot.sensors.Lidar
import org.ghrobotics.robot.subsytems.arm.ArmSubsystem
import org.ghrobotics.robot.subsytems.climber.ClimberSubsystem
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem
import org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem
import org.ghrobotics.robot.subsytems.intake.IntakeSubsystem
import org.ghrobotics.robot.subsytems.led.LEDSubsystem

object Robot : FalconRobotBase(), CoroutineScope {

    override val coroutineContext = Job()

    // Initialize all systems.
    override fun initialize() {
        +DriveSubsystem
        +ArmSubsystem
        +ElevatorSubsystem
        +ClimberSubsystem
        +IntakeSubsystem
        +LEDSubsystem

        NetworkInterface
        Autonomous
        Lidar
    }

    override fun periodic() {
        runBlocking {
            Controls.mainXbox.update()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        RobotBase.startRobot { Robot }
    }
}
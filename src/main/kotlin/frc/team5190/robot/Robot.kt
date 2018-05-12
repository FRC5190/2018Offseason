package frc.team5190.robot

import edu.wpi.first.wpilibj.IterativeRobot
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.auto.Pathreader
import frc.team5190.robot.drive.Drive
import frc.team5190.robot.sensors.Canifier
import frc.team5190.robot.sensors.LEDs

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
        LEDs

        Drive
    }
}
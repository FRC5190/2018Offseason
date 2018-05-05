package frc.team5190.robot.sensors

import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.robot.auto.Pathreader
import frc.team5190.robot.util.setLEDOutput
import java.awt.Color

object LEDs : Subsystem() {
    override fun periodic() {
        Canifier.setLEDOutput(when {
            // Turn LEDs white when all paths have been generated
            Pathreader.pathsGenerated -> Color.WHITE

            // Default case turns LEDs clear
            else -> Color.BLACK

        })
    }

    override fun initDefaultCommand() {

    }
}
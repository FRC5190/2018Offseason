package frc.team5190.robot.sensors

import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.setLEDOutput
import frc.team5190.robot.Robot
import frc.team5190.robot.intake.IntakeSubsystem
import java.awt.Color

object LEDs : Subsystem() {

    private var blinkedFor = 0L

    override fun periodic() {
        Canifier.setLEDOutput(when {
            Robot.INSTANCE.isEnabled -> when {
                Robot.INSTANCE.isClimbing -> {
                    if (System.currentTimeMillis() % 800 > 400) {
                        Color.BLACK
                    } else {
                        Color.ORANGE
                    }
                }
                IntakeSubsystem.isCubeIn -> {
                    if (blinkedFor == 0L) {
                        blinkedFor = System.currentTimeMillis()
                    }
                    if (System.currentTimeMillis() % 400 > 200 && System.currentTimeMillis() - blinkedFor < 2000) {
                        Color.BLACK
                    } else if (Robot.INSTANCE.isAutonomous) {
                        Color.PINK
                    } else {
                        Color.GREEN
                    }
                }
                else -> Color.BLACK
            }
            Robot.INSTANCE.isDisabled -> when {
                Robot.INSTANCE.isAutoReady -> Color.GREEN
                else -> Color.BLACK
            }
            else -> Color.BLACK
        })
    }

    override fun initDefaultCommand() {}
}
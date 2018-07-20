/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.Inches
import frc.team5190.robot.sensors.Lidar
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import java.util.*

class LidarElevatorCommand(private val exit: () -> Boolean = { false }) : Command() {
    init {
        requires(ElevatorSubsystem)
    }

    private val heightBuffer = ArrayDeque<Distance>(3)

    private val heightBufferAverage
        get() = heightBuffer.sumByDouble { it.IN } / 3.0

    override fun execute() {
        if (Lidar.underScale) {
            heightBuffer.add(Inches(Lidar.scaleHeight - 15.0, ElevatorSubsystem.settings))
        }

        ElevatorSubsystem.set(ControlMode.MotionMagic, if (Lidar.underScale) {
            heightBufferAverage.coerceIn(ElevatorSubsystem.Position.FSTAGE.distance.STU.toDouble(),
                    ElevatorSubsystem.Position.HIGHSCALE.distance.STU.toDouble())
        } else {
            ElevatorSubsystem.Position.SCALE.distance.STU.toDouble()
        })
    }

    override fun isFinished() = (!IntakeSubsystem.cubeIn &&
            ElevatorSubsystem.currentPosition >
            ElevatorSubsystem.Position.FSTAGE.distance - Inches(1.0, ElevatorSubsystem.settings)) ||
            exit()
}
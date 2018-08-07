/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.Condition
import frc.team5190.lib.commands.condition
import frc.team5190.lib.commands.or
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.Inches
import frc.team5190.robot.sensors.Lidar
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import java.util.*

class LidarElevatorCommand(exitCondition: Condition = Condition.FALSE) : Command() {
    init {
        +ElevatorSubsystem

        finishCondition += condition {
            !IntakeSubsystem.cubeIn &&
                    ElevatorSubsystem.currentPosition > ElevatorSubsystem.Position.FSTAGE.distance - Inches(1.0, ElevatorSubsystem.settings)
        } or exitCondition
    }

    private val heightBuffer = ArrayDeque<Distance>(3)

    private val heightBufferAverage
        get() = heightBuffer.sumByDouble { it.IN } / 3.0

    override suspend fun execute() {
        if (Lidar.underScale) {
            heightBuffer.add(Inches(Lidar.scaleHeight - 15.0, ElevatorSubsystem.settings))
        }

        ElevatorSubsystem.set(ControlMode.MotionMagic, if (Lidar.underScale) {
            heightBufferAverage.coerceIn(ElevatorSubsystem.Position.FSTAGE.distance.STU.toDouble(),
                    ElevatorSubsystem.Position.SCALE.distance.STU.toDouble())
        } else {
            ElevatorSubsystem.Position.SCALE.distance.STU.toDouble()
        })
    }
}
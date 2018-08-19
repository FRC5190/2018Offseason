/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.and
import frc.team5190.lib.commands.condition
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.Inches
import frc.team5190.lib.utils.not
import frc.team5190.lib.utils.withProcessing
import frc.team5190.robot.sensors.CubeSensors
import frc.team5190.robot.sensors.Lidar
import java.util.*

class LidarElevatorCommand : Command() {
    companion object {
        private val heightOffset = Inches(15.0)
        private val heightSource = Lidar.withProcessing { it.first to it.second.withSettings(ElevatorSubsystem.settings) }
                .withProcessing { it.first to (it.second - heightOffset) }
    }

    init {
        +ElevatorSubsystem

        finishCondition += !CubeSensors.cubeIn and condition {
            ElevatorSubsystem.currentPosition > ElevatorSubsystem.kFirstStagePosition - Inches(1.0, ElevatorSubsystem.settings)
        }
    }

    private val heightBuffer = ArrayDeque<Distance>(3)

    private val heightBufferAverage
        get() = heightBuffer.map { it.IN }.average()

    override suspend fun execute() {
        val (underScale, scaleHeight) = heightSource.value

        if (underScale) heightBuffer.add(scaleHeight)

        ElevatorSubsystem.set(ControlMode.MotionMagic, if (underScale) {
            heightBufferAverage.coerceIn(ElevatorSubsystem.kFirstStagePosition.STU.toDouble(),
                    ElevatorSubsystem.kHighScalePosition.STU.toDouble())
        } else {
            ElevatorSubsystem.kScalePosition.STU.toDouble()
        })
    }
}
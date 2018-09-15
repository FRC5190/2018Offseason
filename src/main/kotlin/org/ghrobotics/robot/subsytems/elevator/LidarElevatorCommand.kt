/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.mathematics.units.Distance
import org.ghrobotics.lib.mathematics.units.Inches
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue
import org.ghrobotics.lib.utils.observabletype.and
import org.ghrobotics.lib.utils.observabletype.not
import org.ghrobotics.robot.sensors.CubeSensors
import org.ghrobotics.robot.sensors.Lidar
import java.util.*

class LidarElevatorCommand : org.ghrobotics.lib.commands.Command(ElevatorSubsystem) {
    companion object {
        private val heightOffset = Inches(15.0)
        private val heightSource = Lidar.withProcessing { it.first to it.second.withSettings(ElevatorSubsystem.settings) }
                .withProcessing { it.first to (it.second - heightOffset) }
    }

    init {
        _finishCondition += !CubeSensors.cubeIn and UpdatableObservableValue {
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
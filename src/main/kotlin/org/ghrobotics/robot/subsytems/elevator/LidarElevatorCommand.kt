/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.elevator

import kotlinx.coroutines.experimental.GlobalScope
import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.mathematics.units.Length
import org.ghrobotics.lib.mathematics.units.inch
import org.ghrobotics.lib.utils.map
import org.ghrobotics.lib.utils.observabletype.and
import org.ghrobotics.lib.utils.observabletype.not
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.sensors.CubeSensors
import org.ghrobotics.robot.sensors.Lidar
/* ktlint-disable no-wildcard-imports */
import java.util.*

class LidarElevatorCommand : FalconCommand(ElevatorSubsystem) {
    companion object {
        private val heightOffset = 15.inch
        private val heightSource = Lidar.map { it.first to (it.second - heightOffset) }
    }

    private var heightNeeded = 0.inch

    override fun CreateCommandScope.create() {
        finishCondition += !CubeSensors.cubeIn and GlobalScope.updatableValue {
            (ElevatorSubsystem.elevatorPosition - heightNeeded).absoluteValue < Constants.kElevatorClosedLpTolerance
        }
    }

    override suspend fun InitCommandScope.initialize() {
        heightNeeded = ElevatorSubsystem.kScalePosition
    }

    private val heightBuffer = ArrayDeque<Length>(3)

    private val heightBufferAverage
        get() = heightBuffer.map { it.inch.asDouble }.average().inch

    override suspend fun execute() {
        val (underScale, scaleHeight) = heightSource()

        if (underScale) heightBuffer.add(scaleHeight)

        heightNeeded = if (underScale) {
            heightBufferAverage.coerceIn(
                    ElevatorSubsystem.kFirstStagePosition,
                    ElevatorSubsystem.kHighScalePosition
            )
        } else {
            ElevatorSubsystem.kScalePosition
        }

        ElevatorSubsystem.elevatorPosition = heightNeeded
    }
}
package org.ghrobotics.robot.sensors

import edu.wpi.first.wpilibj.AnalogInput
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.and
import org.ghrobotics.lib.utils.greaterThan
import org.ghrobotics.robot.Constants

object CubeSensors {

    private val leftCubeSensor: Source<Double> = AnalogInput(Constants.kLeftCubeSensorId).let { { it.averageVoltage } }
    private val rightCubeSensor: Source<Double> = AnalogInput(Constants.kRightCubeSensorId).let { { it.averageVoltage } }

    private const val kVoltThreshold = 0.9

    val cubeIn = leftCubeSensor.greaterThan(kVoltThreshold) and rightCubeSensor.greaterThan(kVoltThreshold)
}
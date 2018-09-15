package org.ghrobotics.robot.sensors

import edu.wpi.first.wpilibj.AnalogInput
import org.ghrobotics.lib.utils.observabletype.and
import org.ghrobotics.lib.utils.observabletype.asObservableVoltage
import org.ghrobotics.robot.Constants

object CubeSensors {

    private val leftCubeSensor = AnalogInput(Constants.kLeftCubeSensorId).asObservableVoltage()
    private val rightCubeSensor = AnalogInput(Constants.kRightCubeSensorId).asObservableVoltage()

    private const val kVoltThreshold = 0.9

    val cubeIn = leftCubeSensor.greaterThan(kVoltThreshold) and rightCubeSensor.greaterThan(kVoltThreshold)

}
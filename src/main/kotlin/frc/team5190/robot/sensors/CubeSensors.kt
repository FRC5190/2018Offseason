package frc.team5190.robot.sensors

import edu.wpi.first.wpilibj.AnalogInput
import frc.team5190.lib.utils.observabletype.and
import frc.team5190.lib.utils.observabletype.asObservableVoltage
import frc.team5190.robot.Constants

object CubeSensors {

    private val leftCubeSensor = AnalogInput(Constants.kLeftCubeSensorId).asObservableVoltage()
    private val rightCubeSensor = AnalogInput(Constants.kRightCubeSensorId).asObservableVoltage()

    private const val kVoltThreshold = 0.9

    val cubeIn = leftCubeSensor.greaterThan(kVoltThreshold) and rightCubeSensor.greaterThan(kVoltThreshold)

}
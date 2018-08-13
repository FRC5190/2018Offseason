package frc.team5190.robot.sensors

import edu.wpi.first.wpilibj.AnalogInput
import frc.team5190.lib.utils.BooleanState
import frc.team5190.lib.utils.StateImpl
import frc.team5190.lib.utils.updatableState
import frc.team5190.robot.Constants
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object CubeSensors {

    private val leftCubeSensor = AnalogInput(Constants.kLeftCubeSensorId)
    private val rightCubeSensor = AnalogInput(Constants.kRightCubeSensorId)

    private const val kVoltThreshold = 0.9

    val cubeIn: BooleanState = updatableState { leftCubeSensor.voltage > kVoltThreshold && rightCubeSensor.voltage > kVoltThreshold }

}
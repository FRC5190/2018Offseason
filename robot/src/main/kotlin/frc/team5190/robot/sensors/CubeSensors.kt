package frc.team5190.robot.sensors

import edu.wpi.first.wpilibj.AnalogInput
import frc.team5190.lib.commands.and
import frc.team5190.lib.utils.*
import frc.team5190.robot.Constants
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object CubeSensors {

    private val leftCubeSensor = AnalogInput(Constants.kLeftCubeSensorId).voltageState
    private val rightCubeSensor = AnalogInput(Constants.kRightCubeSensorId).voltageState

    private const val kVoltThreshold = 0.9

    val cubeIn: BooleanState = processedState(leftCubeSensor) { it > kVoltThreshold} and processedState(rightCubeSensor) { it > kVoltThreshold }

}
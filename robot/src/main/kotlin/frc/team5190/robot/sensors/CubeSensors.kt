package frc.team5190.robot.sensors

import edu.wpi.first.wpilibj.AnalogInput
import frc.team5190.lib.commands.Condition
import frc.team5190.robot.Constants
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object CubeSensors {

    private val leftCubeSensor = AnalogInput(Constants.kLeftCubeSensorId)
    private val rightCubeSensor = AnalogInput(Constants.kRightCubeSensorId)

    private const val kVoltThreshold = 0.9

    val cubeIn: Condition = CubeCondition(false)
    val cubeInInverted: Condition = CubeCondition(true)

    private class CubeCondition(private val inverted: Boolean) : Condition() {
        private var lastState = false

        init {
            launch {
                while (isActive) {
                    val newState = leftCubeSensor.voltage > kVoltThreshold && rightCubeSensor.voltage > kVoltThreshold
                    if ((!inverted && !lastState && newState) || (inverted && lastState && !newState)) invokeCompletionListeners()
                    lastState = newState
                    delay(20)
                }
            }
        }

        override fun not() = if (inverted) cubeIn else cubeInInverted

        override fun isMet() = if (inverted) !lastState else lastState
    }

}
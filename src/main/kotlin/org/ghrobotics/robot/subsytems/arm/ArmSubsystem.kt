/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import org.ghrobotics.lib.commands.Subsystem
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.nativeunits.STU
import org.ghrobotics.lib.wrappers.FalconSRX
import org.ghrobotics.robot.Constants

object ArmSubsystem : Subsystem() {

    private val armMaster = FalconSRX(Constants.kArmId, Constants.kArmNativeUnitModel)

    val kDownPosition = Constants.kArmDownPosition
    val kMiddlePosition = kDownPosition + 40.STU.toModel(Constants.kArmNativeUnitModel)
    val kUpPosition = kDownPosition + 200.STU.toModel(Constants.kArmNativeUnitModel)
    val kAllUpPosition = kDownPosition + 250.STU.toModel(Constants.kArmNativeUnitModel)
    val kBehindPosition = kDownPosition + 380.STU.toModel(Constants.kArmNativeUnitModel)

    var armPosition
        get() = armMaster.sensorPosition
        set(value) {
            armMaster.set(ControlMode.MotionMagic, value)
        }

    init {
        armMaster.apply {
            inverted = true
            encoderPhase = false
            feedbackSensor = FeedbackDevice.Analog

            peakForwardOutput = 1.0
            peakReverseOutput = -1.0

            kP = Constants.kPArm
            kF = Constants.kVArm
            allowedClosedLoopError = Constants.kArmClosedLpTolerance

            continuousCurrentLimit = 20.amp
            currentLimitingEnabled = true

            motionCruiseVelocity = Constants.kArmMotionMagicVelocity
            motionAcceleration = Constants.kArmMotionMagicAcceleration

            brakeMode = NeutralMode.Brake
        }

        defaultCommand = ClosedLoopArmCommand()
    }

    fun set(controlMode: ControlMode, output: Double) = armMaster.set(controlMode, output)
}
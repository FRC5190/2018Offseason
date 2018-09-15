/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import org.ghrobotics.lib.commands.Subsystem
import org.ghrobotics.lib.mathematics.units.Amps
import org.ghrobotics.lib.mathematics.units.Distance
import org.ghrobotics.lib.mathematics.units.NativeUnits
import org.ghrobotics.lib.wrappers.FalconSRX
import org.ghrobotics.robot.Constants

object ArmSubsystem : Subsystem() {
    private val armMaster = FalconSRX(Constants.kArmId)

    val kDownPosition = Constants.kArmDownPosition
    val kMiddlePosition = kDownPosition + NativeUnits(40)
    val kUpPosition = kDownPosition + NativeUnits(200)
    val kAllUpPosition = kDownPosition + NativeUnits(250)
    val kBehindPosition = kDownPosition + NativeUnits(380)

    val currentPosition: Distance
        get() = armMaster.sensorPosition

    init {
        armMaster.apply {
            inverted = true
            encoderPhase = false
            feedbackSensor = FeedbackDevice.Analog

            peakFwdOutput = 1.0
            peakRevOutput = -1.0

            kP = Constants.kPArm
            kF = Constants.kVArm
            closedLoopTolerance = Constants.kArmClosedLpTolerance

            continuousCurrentLimit = Amps(20)
            currentLimitingEnabled = true

            motionCruiseVelocity = Constants.kArmMotionMagicVelocity
            motionAcceleration = Constants.kArmMotionMagicAcceleration

            brakeMode = NeutralMode.Brake
        }

        defaultCommand = ClosedLoopArmCommand()
    }

    fun set(controlMode: ControlMode, output: Double) = armMaster.set(controlMode, output)
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.math.units.Amps
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.NativeUnits
import frc.team5190.lib.utils.launchFrequency
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

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
            inverted       = true
            encoderPhase   = false
            feedbackSensor = FeedbackDevice.Analog

            peakFwdOutput = 1.0
            peakRevOutput = -1.0

            kP                  = Constants.kPArm
            kF                  = Constants.kVArm
            closedLoopTolerance = Constants.kArmClosedLpTolerance

            continuousCurrentLimit  = Amps(20)
            currentLimitingEnabled = true

            motionCruiseVelocity = Constants.kArmMotionMagicVelocity
            motionAcceleration   = Constants.kArmMotionMagicAcceleration

            brakeMode = NeutralMode.Brake
        }

        defaultCommand = ClosedLoopArmCommand()
    }

    fun set(controlMode: ControlMode, output: Double) = armMaster.set(controlMode, output)
}
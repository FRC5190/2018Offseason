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
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

object ArmSubsystem : Subsystem() {
    private val armMaster = FalconSRX(Constants.kArmId)

    val currentPosition: Distance
        get() = armMaster.sensorPosition

    init {
        armMaster.apply {
            inverted       = true
            encoderPhase   = false
            feedbackSensor = FeedbackDevice.Analog
            peakFwdOutput = 0.3
            peakRevOutput = -0.3

            kP                  = Constants.kPArm
            closedLoopTolerance = Constants.kArmClosedLpTolerance

            continousCurrentLimit  = Amps(20)
            currentLimitingEnabled = true

            motionCruiseVelocity = Constants.kArmMotionMagicVelocity
            motionAcceleration   = Constants.kArmMotionMagicAcceleration

            brakeMode = NeutralMode.Brake
        }

        defaultCommand = ClosedLoopArmCommand()
    }

    fun set(controlMode: ControlMode, output: Double) = armMaster.set(controlMode, output)

    enum class Position(val distance: Distance) {
        DOWN  (Constants.kArmDownPosition),
        MIDDLE(Constants.kArmDownPosition + NativeUnits(40)),
        UP    (Constants.kArmDownPosition + NativeUnits(200)),
        ALLUP (Constants.kArmDownPosition + NativeUnits(250)),
        BEHIND(Constants.kArmDownPosition + NativeUnits(380))
    }
}
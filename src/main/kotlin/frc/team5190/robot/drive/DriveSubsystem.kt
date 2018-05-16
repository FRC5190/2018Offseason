package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.units.*
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.MotorIDs


object DriveSubsystem : Subsystem() {

    private val frontLeft = FalconSRX(MotorIDs.FRONT_LEFT)
    private val frontRight = FalconSRX(MotorIDs.FRONT_RIGHT)

    private val rearLeft = FalconSRX(MotorIDs.REAR_LEFT)
    private val rearRight = FalconSRX(MotorIDs.REAR_RIGHT)

    private val allMasters = arrayOf(frontLeft, frontRight)

    private val leftMotors = arrayOf(frontLeft, rearLeft)
    private val rightMotors = arrayOf(frontRight, rearRight)

    private val allMotors = arrayOf(*leftMotors, *rightMotors)

    val leftPosition: Distance
        get() = frontLeft.sensorPosition

    val rightPosition: Distance
        get() = frontRight.sensorPosition

    val leftVelocity: Speed
        get() = frontLeft.sensorVelocity

    val rightVelocity: Speed
        get() = frontLeft.sensorVelocity

    init {
        rearLeft.follow(frontLeft)
        rearRight.follow(frontRight)

        leftMotors.forEach { it.inverted = true }
        rightMotors.forEach { it.inverted = false }

        allMasters.forEach {
            it.feedbackSensor = FeedbackDevice.QuadEncoder
            it.encoderPhase = false
        }

        allMotors.forEach {
            it.peakFwdOutput = 1.0
            it.peakRevOutput = -1.0

            it.voltageCompensationSaturation = Volts(12.0)
            it.voltageCompensationEnabled = true

            it.peakCurrentLimit = Amps(50)
            it.peakCurrentLimitDuration = Milliseconds(0)
            it.continousCurrentLimit = Amps(50)
            it.currentLimitingEnabled = true
        }

    }

    fun set(controlMode: ControlMode, leftOutput: Double, rightOutput: Double) {
        frontLeft.set(controlMode, leftOutput)
        frontRight.set(controlMode, rightOutput)
    }

    fun resetEncoders() {
        allMasters.forEach {
            it.sensorPosition = NativeUnits(0)
        }
    }

    override fun initDefaultCommand() {
        defaultCommand = DriveCommand()
    }

}
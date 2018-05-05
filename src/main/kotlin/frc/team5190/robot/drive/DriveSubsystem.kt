package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

object DriveSubsystem : Subsystem() {

    private val frontLeft = FalconSRX(Constants.MotorIDs.FRONT_LEFT)
    private val frontRight = FalconSRX(Constants.MotorIDs.FRONT_RIGHT)

    private val rearLeft = FalconSRX(Constants.MotorIDs.REAR_LEFT)
    private val rearRight = FalconSRX(Constants.MotorIDs.REAR_RIGHT)

    private val masters = arrayOf(frontLeft, frontRight)

    private val leftMotors = arrayOf(frontLeft, rearLeft)
    private val rightMotors = arrayOf(frontRight, rearRight)

    val leftEncoderPosition
        get() = frontLeft.sensorPosition

    val rightEncoderPosition
        get() = frontRight.sensorPosition

    val leftVelocity
        get() = frontLeft.sensorVelocity

    val rightVelocity
        get() = frontLeft.sensorVelocity

    init {
        leftMotors.forEach { it.inverted = true }
        rightMotors.forEach { it.inverted = false }

        masters.forEach {
            it.feedbackSensor = FeedbackDevice.QuadEncoder

            it.peakFwdOutput = 1.0
            it.peakRevOutput = -1.0

            it.encoderPhase = false
        }

        rearLeft.follow(frontLeft)
        rearRight.follow(frontRight)
    }

    fun set(controlMode: ControlMode, leftOutput: Double, rightOutput: Double) {
        frontLeft.set(controlMode, leftOutput)
        frontRight.set(controlMode, rightOutput)
    }

    fun resetEncoders() {
        masters.forEach {
            it.sensorPosition = 0
        }
    }


    override fun initDefaultCommand() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
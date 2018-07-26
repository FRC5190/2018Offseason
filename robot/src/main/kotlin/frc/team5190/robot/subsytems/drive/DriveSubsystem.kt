/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.math.units.*
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants


object DriveSubsystem : Subsystem() {

    private val leftMaster = FalconSRX(Constants.kLeftMasterId)
    private val rightMaster = FalconSRX(Constants.kRightMasterId)

    private val leftSlave1 = FalconSRX(Constants.kLeftSlaveId1)
    private val rightSlave1 = FalconSRX(Constants.kRightSlaveId1)

    private val allMasters = arrayOf(leftMaster, rightMaster)

    private val leftMotors = arrayOf(leftMaster, leftSlave1)
    private val rightMotors = arrayOf(rightMaster, rightSlave1)

    private val allMotors = arrayOf(*leftMotors, *rightMotors)

    val leftPosition: Distance
        get() = leftMaster.sensorPosition

    val rightPosition: Distance
        get() = rightMaster.sensorPosition

    val leftVelocity: Speed
        get() = leftMaster.sensorVelocity

    val rightVelocity: Speed
        get() = rightMaster.sensorVelocity

    val leftPercent: Double
        get() = leftMaster.motorOutputPercent

    val rightPercent: Double
        get() = rightMaster.motorOutputPercent

    val leftAmperage: Double
        get() = leftMaster.outputCurrent

    val rightAmperage: Double
        get() = rightMaster.outputCurrent


    init {
        arrayListOf(leftSlave1).forEach {
            it.follow(leftMaster)
            it.inverted = false
        }
        arrayListOf(rightSlave1).forEach {
            it.follow(rightMaster)
            it.inverted = true
        }

        leftMotors.forEach { it.apply { it.inverted = false }  }
        rightMotors.forEach { it.apply { it.inverted = true } }

        allMasters.forEach {
            it.feedbackSensor = FeedbackDevice.QuadEncoder
            it.encoderPhase = false
        }

        allMotors.forEach {
            it.peakFwdOutput = 1.0
            it.peakRevOutput = -1.0

            it.nominalFwdOutput = 0.0
            it.nominalRevOutput = 0.0

            it.brakeMode = NeutralMode.Brake

            it.voltageCompensationSaturation = Volts(12.0)
            it.voltageCompensationEnabled = true

            it.peakCurrentLimit = Amps(0)
            it.peakCurrentLimitDuration = Milliseconds(0)
            it.continousCurrentLimit = Amps(40)
            it.currentLimitingEnabled = true
        }
        resetEncoders()

        defaultCommand = ManualDriveCommand()
    }

    fun set(controlMode: ControlMode, leftOutput: Double, rightOutput: Double) {
        leftMaster.set(controlMode, leftOutput)
        rightMaster.set(controlMode, rightOutput)
    }

    fun resetEncoders() {
        allMasters.forEach {
            it.sensorPosition = NativeUnits(0)
        }
    }
}
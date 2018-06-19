package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.units.*
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.DriveConstants
import frc.team5190.robot.MotorIDs


object DriveSubsystem : Subsystem() {

    private val leftMaster = FalconSRX(MotorIDs.LEFT_MASTER)
    private val rightMaster = FalconSRX(MotorIDs.RIGHT_MASTER)

    private val leftSlave1 = FalconSRX(MotorIDs.LEFT_SLAVE_1)
    private val rightSlave1 = FalconSRX(MotorIDs.RIGHT_SLAVE_1)

    private val leftSlave2 = FalconSRX(MotorIDs.LEFT_SLAVE_2)
    private val rightSlave2 = FalconSRX(MotorIDs.RIGHT_SLAVE_2)

    private val allMasters = arrayOf(leftMaster, rightMaster)

    private val leftMotors = arrayOf(leftMaster, leftSlave1, leftSlave2)
    private val rightMotors = arrayOf(rightMaster, rightSlave1, rightSlave2)

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
        arrayListOf(leftSlave1, leftSlave2).forEach {
            it.follow(leftMaster)
            it.inverted = false
        }
        arrayListOf(rightSlave1, rightSlave2).forEach {
            it.follow(rightMaster)
            it.inverted = true
        }

        leftMaster.apply {
            inverted = true
        }
        rightMaster.apply {
            inverted = false
        }

        allMasters.forEach {
            it.feedbackSensor = FeedbackDevice.QuadEncoder
            it.encoderPhase = true
        }

        allMotors.forEach { srx ->
            srx.peakFwdOutput = 1.0
            srx.peakRevOutput = -1.0

            srx.nominalFwdOutput = 0.0
            srx.nominalRevOutput = 0.0

            srx.brakeMode = NeutralMode.Brake

            srx.voltageCompensationSaturation = Volts(12.0)
            srx.voltageCompensationEnabled = true

            srx.peakCurrentLimit = Amps(0)
            srx.peakCurrentLimitDuration = Milliseconds(0)
            srx.continousCurrentLimit = Amps(40)
            srx.currentLimitingEnabled = true
        }
        resetEncoders()
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

    override fun initDefaultCommand() {
        defaultCommand = ManualDriveCommand()
    }
}
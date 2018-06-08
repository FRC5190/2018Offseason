package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.Solenoid
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.units.*
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.MotorIDs
import frc.team5190.robot.SolenoidIDs


object DriveSubsystem : Subsystem() {

    private val frontLeft = FalconSRX(MotorIDs.FRONT_LEFT)
    private val frontRight = FalconSRX(MotorIDs.FRONT_RIGHT)

    private val rearLeft = FalconSRX(MotorIDs.REAR_LEFT)
    private val rearRight = FalconSRX(MotorIDs.REAR_RIGHT)

    private val rearLeft2 = FalconSRX(40)
    private val rearRight2 = FalconSRX(41)

    private val allMasters = arrayOf(frontLeft, frontRight)

    private val leftMotors = arrayOf(frontLeft, rearLeft, rearLeft2)
    private val rightMotors = arrayOf(frontRight, rearRight, rearRight2)

    private val allMotors = arrayOf(*leftMotors, *rightMotors)

    private val gearSolenoid = Solenoid(SolenoidIDs.PCM, SolenoidIDs.DRIVE)

    var gear: Gear
        get() = Gear.getGear(gearSolenoid.get())
        set(value) {
            when (value) {
                Gear.HIGH -> allMasters.forEach {
                    it.openLoopRamp = Seconds(0.0)
                }
                Gear.LOW -> allMasters.forEach {
                   it.openLoopRamp = Seconds(0.2)
                }
            }
            gearSolenoid.set(value.state)
        }

    val leftPosition: Distance
        get() = frontLeft.sensorPosition

    val rightPosition: Distance
        get() = frontRight.sensorPosition

    val leftVelocity: Speed
        get() = frontLeft.sensorVelocity

    val rightVelocity: Speed
        get() = frontLeft.sensorVelocity

    val leftPercent: Double
        get() = frontLeft.motorOutputPercent

    val rightPercent: Double
        get() = frontRight.motorOutputPercent

    val leftAmperage: Double
        get() = frontLeft.outputCurrent

    val rightAmperage: Double
        get() = frontRight.outputCurrent


    init {
        rearLeft.follow(frontLeft)
        rearRight.follow(frontRight)
        rearLeft2.follow(frontLeft)
        rearRight2.follow(frontRight)


        leftMotors.forEach { it.inverted = false }
        rightMotors.forEach { it.inverted = true }

        rearRight.inverted = false
        rearLeft2.inverted = true


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
        defaultCommand = ManualDriveCommand()
    }
}

enum class Gear(val state: Boolean) {
    HIGH(false), LOW(true);

    companion object {
        fun getGear(solenoidState: Boolean) = Gear.values().find { it.state == solenoidState }!!
    }
}
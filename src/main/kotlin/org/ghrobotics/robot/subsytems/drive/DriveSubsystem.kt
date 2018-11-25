/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.team254.lib.physics.DCMotorTransmission
import com.team254.lib.physics.DifferentialDrive
import edu.wpi.first.wpilibj.Solenoid
import org.ghrobotics.lib.mathematics.twodim.control.RamseteController
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.derivedunits.LinearVelocity
import org.ghrobotics.lib.mathematics.units.derivedunits.volt
import org.ghrobotics.lib.mathematics.units.meter
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.subsystems.drive.TankDriveSubsystem
import org.ghrobotics.lib.wrappers.FalconSRX
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.sensors.AHRS
import kotlin.math.pow

object DriveSubsystem : TankDriveSubsystem() {

    override val leftMaster = FalconSRX(Constants.kLeftMasterId, Constants.kDriveNativeUnitModel)
    override val rightMaster = FalconSRX(Constants.kRightMasterId, Constants.kDriveNativeUnitModel)

    private val leftSlave1 = FalconSRX(Constants.kLeftSlaveId1, Constants.kDriveNativeUnitModel)
    private val rightSlave1 = FalconSRX(Constants.kRightSlaveId1, Constants.kDriveNativeUnitModel)

    private val allMasters = arrayOf(leftMaster, rightMaster)

    private val leftMotors = arrayOf(leftMaster, leftSlave1)
    private val rightMotors = arrayOf(rightMaster, rightSlave1)

    private val allMotors = arrayOf(*leftMotors, *rightMotors)

    override val ahrsSensor = AHRS

    private val shifter = Solenoid(Constants.kPCMId, Constants.kDriveSolenoidId)


    // Torque per volt derivation
    // Ka is in radians per second per second. Multiply by wheel radius to get in meters per second per second.
    // Multiply acceleration by robot mass to get force.
    // Multiply force by wheel radius to get torque.
    // Divide by 2 to get the torque for one side because the Ka is for the overall robot.

    private val transmission = DCMotorTransmission(
            1 / Constants.kVDrive,
            Constants.kWheelRadius.value.pow(2) * Constants.kRobotMass / (2.0 * Constants.kADrive),
            Constants.kStaticFrictionVoltage
    )

    val driveModel = DifferentialDrive(
            Constants.kRobotMass,
            Constants.kRobotMomentOfInertia,
            Constants.kRobotAngularDrag,
            Constants.kWheelRadius.value,
            Constants.kTrackWidth.value / 2.0,
            transmission,
            transmission
    )

    override val trajectoryFollower = RamseteController(
            driveModel,
            Constants.kDriveBeta,
            Constants.kDriveZeta
    )

    var lowGear = false
        set(wantLow) {
            if (wantLow) {
                shifter.set(true)
                resetLowGear()
            } else {
                shifter.set(false)
                resetHighGear()
            }
            field = wantLow
        }


    init {
        lowGear = false

        mutableListOf(leftSlave1).forEach {
            it.follow(leftMaster)
            it.inverted = false
        }
        mutableListOf(rightSlave1).forEach {
            it.follow(rightMaster)
            it.inverted = true
        }

        leftMotors.forEach { it.inverted = false }
        rightMotors.forEach { it.inverted = true }

        allMasters.forEach {
            it.feedbackSensor = FeedbackDevice.QuadEncoder
            it.encoderPhase = false
        }

        resetHighGear()

        allMotors.forEach {
            it.peakForwardOutput = 1.0
            it.peakReverseOutput = -1.0

            it.nominalForwardOutput = 0.0
            it.nominalReverseOutput = 0.0

            it.brakeMode = NeutralMode.Brake

            it.voltageCompensationSaturation = 12.volt
            it.voltageCompensationEnabled = true

            it.peakCurrentLimit = 0.amp
            it.peakCurrentLimitDuration = 0.millisecond
            it.continuousCurrentLimit = 40.amp
            it.currentLimitingEnabled = true
        }
        // Zero the encoders once on start up
        allMasters.forEach { it.sensorPosition = 0.meter }

        defaultCommand = ManualDriveCommand()
    }

    private fun resetHighGear() {
        allMasters.forEach {
            it.kP = Constants.kPDrive
            it.kD = Constants.kDDrive
            it.kF = 0.0
            it.openLoopRamp = 0.second
        }
    }

    private fun resetLowGear() {
        allMasters.forEach {
            it.kP = 0.0
            it.kF = 0.0
            it.openLoopRamp = 0.2.second
        }
    }

    override fun zeroOutputs() {
        set(ControlMode.PercentOutput, 0.0, 0.0)
    }

    fun set(controlMode: ControlMode, leftOutput: Double, rightOutput: Double) {
        leftMaster.set(controlMode, leftOutput)
        rightMaster.set(controlMode, rightOutput)
    }
}
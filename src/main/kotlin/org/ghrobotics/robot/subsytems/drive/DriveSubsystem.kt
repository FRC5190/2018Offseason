/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.Solenoid
import org.ghrobotics.lib.commands.Subsystem
import org.ghrobotics.lib.mathematics.twodim.control.TrajectoryFollower
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.derivedunits.Velocity
import org.ghrobotics.lib.mathematics.units.derivedunits.volt
import org.ghrobotics.lib.mathematics.units.meter
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.wrappers.FalconSRX
import org.ghrobotics.robot.Constants


object DriveSubsystem : Subsystem() {

    private val leftMaster = FalconSRX(Constants.kLeftMasterId, Constants.kDriveNativeUnitModel)
    private val rightMaster = FalconSRX(Constants.kRightMasterId, Constants.kDriveNativeUnitModel)

    private val leftSlave1 = FalconSRX(Constants.kLeftSlaveId1, Constants.kDriveNativeUnitModel)
    private val rightSlave1 = FalconSRX(Constants.kRightSlaveId1, Constants.kDriveNativeUnitModel)

    private val allMasters = arrayOf(leftMaster, rightMaster)

    private val leftMotors = arrayOf(leftMaster, leftSlave1)
    private val rightMotors = arrayOf(rightMaster, rightSlave1)

    private val allMotors = arrayOf(*leftMotors, *rightMotors)

    private val shifter = Solenoid(Constants.kPCMId, Constants.kDriveSolenoidId)

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


    val leftPosition
        get() = leftMaster.sensorPosition

    val rightPosition
        get() = rightMaster.sensorPosition

    val leftVelocity: Velocity
        get() = leftMaster.sensorVelocity

    val rightVelocity: Velocity
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
        lowGear = false

        arrayListOf(leftSlave1).forEach {
            it.follow(leftMaster)
            it.inverted = false
        }
        arrayListOf(rightSlave1).forEach {
            it.follow(rightMaster)
            it.inverted = true
        }

        leftMotors.forEach { it.apply { it.inverted = false } }
        rightMotors.forEach { it.apply { it.inverted = true } }

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
        resetEncoders()

        defaultCommand = ManualDriveCommand()
    }

    private fun resetHighGear() {
        allMasters.forEach {
            it.kP = Constants.kPDrive
            it.kF = Constants.kVDrive
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

    fun set(controlMode: ControlMode, leftOutput: Double, rightOutput: Double) {
        leftMaster.set(controlMode, leftOutput)
        rightMaster.set(controlMode, rightOutput)
    }

    fun setTrajectoryVelocity(pathOut: TrajectoryFollower.Output) {
        leftMaster.set(ControlMode.Velocity, pathOut.lSetpoint, DemandType.ArbitraryFeedForward, pathOut.lfVoltage.asDouble / 12.0)
        rightMaster.set(ControlMode.Velocity, pathOut.rSetpoint, DemandType.ArbitraryFeedForward, pathOut.rfVoltage.asDouble / 12.0)
    }

    fun resetEncoders() {
        allMasters.forEach {
            it.sensorPosition = 0.meter
        }
    }
}
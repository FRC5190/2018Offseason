/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.sensors.PigeonIMU
import com.team254.lib.physics.DCMotorTransmission
import com.team254.lib.physics.DifferentialDrive
import edu.wpi.first.wpilibj.Solenoid
import org.ghrobotics.lib.localization.TankEncoderLocalization
import org.ghrobotics.lib.mathematics.twodim.control.RamseteTracker
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.sensors.asSource
import org.ghrobotics.lib.subsystems.drive.TankDriveSubsystem
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.Robot
import kotlin.math.pow
import kotlin.properties.Delegates

object DriveSubsystem : TankDriveSubsystem() {

    // Gearboxes
    private val leftGearbox = DriveGearbox(
        Constants.kLeftMasterId,
        Constants.kLeftSlaveId1,
        false, false
    )
    private val rightGearbox = DriveGearbox(
        Constants.kRightMasterId,
        Constants.kRightSlaveId1,
        true, false
    )

    // Master motors
    override val leftMotor get() = leftGearbox.master
    override val rightMotor get() = rightGearbox.master

    private val allMasters get() = listOf(leftMotor, rightMotor)


    // Shifter for two-speed gearbox
    private val shifter = Solenoid(Constants.kPCMId, Constants.kDriveSolenoidId)

    // Type of localization to determine position on the field
    override val localization = TankEncoderLocalization(
        PigeonIMU(17).asSource(), { leftMotor.sensorPosition }, { rightMotor.sensorPosition },
        Robot.coroutineContext
    )

    // Shift up and down
    var lowGear by Delegates.observable(false) { _, _, wantLow ->
        if (wantLow) {
            shifter.set(true)
        } else {
            shifter.set(false)
        }
    }


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

    override val differentialDrive = DifferentialDrive(
        Constants.kRobotMass,
        Constants.kRobotMomentOfInertia,
        Constants.kRobotAngularDrag,
        Constants.kWheelRadius.value,
        Constants.kTrackWidth.value / 2.0,
        transmission,
        transmission
    )

    override val trajectoryTracker = RamseteTracker(Constants.kDriveBeta, Constants.kDriveZeta)

    init {
        lowGear = false
        defaultCommand = ManualDriveCommand()
        resetHighGear()
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
        leftMotor.set(controlMode, leftOutput)
        rightMotor.set(controlMode, rightOutput)
    }
}
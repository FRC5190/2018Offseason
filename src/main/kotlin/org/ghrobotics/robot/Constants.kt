/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot

import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.*
import org.ghrobotics.lib.mathematics.units.derivedunits.acceleration
import org.ghrobotics.lib.mathematics.units.derivedunits.velocity
import org.ghrobotics.lib.mathematics.units.expressions.SIExp2
import org.ghrobotics.lib.mathematics.units.fractions.SIFrac11
import org.ghrobotics.lib.mathematics.units.fractions.SIFrac12
import org.ghrobotics.lib.mathematics.units.nativeunits.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Constants {

    // GLOBAL CTRE TIMEOUT
    const val kCTRETimeout = 10


    // MOTOR IDS
    const val kLeftMasterId = 1
    const val kLeftSlaveId1 = 2

    const val kRightMasterId = 3
    const val kRightSlaveId1 = 4

    const val kElevatorMasterId = 5
    const val kElevatorSlaveId = 6

    const val kIntakeMasterId = 7
    const val kIntakeSlaveId = 9

    const val kArmId = 8

    const val kWinchMasterId = 10
    const val kWinchSlaveId = 59


    // CANIFIER
    const val kCANifierId = 16


    // PNEUMATICS
    const val kPCMId = 41
    const val kDriveSolenoidId = 3
    const val kIntakeSolenoidId = 2


    // SERVO
    const val kLidarServoId = 0


    // ANALOG INPUT
    const val kLeftCubeSensorId = 2
    const val kRightCubeSensorId = 3


    // ROBOT
    val kRobotWidth = 27.inch
    val kRobotLength = 33.inch
    val kIntakeLength = 16.0.inch
    val kBumperLength = 2.0.inch

    val kRobotStartX = (kRobotLength / 2.0) + kBumperLength

    val kExchangeZoneBottomY = 14.5.feet
    val kPortalZoneBottomY = (27 - (29.69 / 12.0)).feet

    val kRobotSideStartY = kPortalZoneBottomY - (kRobotWidth / 2.0) - kBumperLength
    val kRobotCenterStartY = kExchangeZoneBottomY - (kRobotWidth / 2.0) - kBumperLength

    const val kRobotMass = 54.53 /* Robot */ + 5.669 /* Battery */ + 7 /* Bumpers */ // kg
    const val kRobotMomentOfInertia = 10.0 // kg m^2
    const val kRobotAngularDrag = 12.0 // N*m / (rad/sec)

    // MECHANISM TRANSFORMATIONS
    val kFrontToIntake = Pose2d(-kIntakeLength, 0.meter, 0.degree)
    val kCenterToIntake = Pose2d(-(kRobotLength / 2.0) - kIntakeLength, 0.meter, 0.degree)
    val kCenterToFrontBumper = Pose2d(-(kRobotLength / 2.0) - kBumperLength, 0.meter, 0.degree)


    // DRIVE
    val kDriveSensorUnitsPerRotation = 1440.STU
    val kWheelRadius = 2.92.inch
    val kTrackWidth = 2.6.feet

    val kDriveNativeUnitModel = NativeUnitLengthModel(
            kDriveSensorUnitsPerRotation,
            kWheelRadius
    )

    const val kPDrive = 2.0 // Talon SRX Units

    const val kStaticFrictionVoltage = 1.2 // Volts
    const val kVDrive = 0.173 // Volts per radians per second
    const val kADrive = 0.020 // Volts per radians per second per second


    const val kDriveBeta = 1.68 // Inverse meters squared
    const val kDriveZeta = 0.85 // Unitless dampening co-efficient


    // ARM
    val kArmNativeUnitModel = NativeUnitRotationModel()

    val kArmDownPosition = (-795).STU.toModel(kArmNativeUnitModel)

    const val kPArm = 4.5
    const val kVArm = 16.78 + 0.9 // 1023 units per STU (velocity)

    val kArmMotionMagicVelocity = SIFrac11(1000000.degree, 1.second)
    val kArmMotionMagicAcceleration = SIFrac12(400.STU.toModel(kArmNativeUnitModel), SIExp2(1.second, 1.second))
    val kArmClosedLpTolerance = 14.degree

    val kArmAutoTolerance = 35.degree


    // ELEVATOR
    const val kPElevator = 0.3
    const val kVElevator = 0.395 // 1023 units per STU (velocity)

    val elevatorNativeUnitSettings = NativeUnitLengthModel(
            1440.STU,
            1.25.inch / 2.0
    )

    val kElevatorSoftLimitFwd = 22500.STU
    val kElevatorClosedLpTolerance = 1.inch

    val kElevatorMotionMagicVelocity = 72.inch.velocity
    val kElevatorMotionMagicAcceleration = 90.inch.acceleration


    // CLIMBER
    const val kPClimber = 2.0

    val kClimberClosedLpTolerance = 1000.STU

    val kClimberMotionMagicVelocity = 1000000.STUPer100ms
    val kClimberMotionMagicAcceleration = 12000.STUPer100msPerSecond
}

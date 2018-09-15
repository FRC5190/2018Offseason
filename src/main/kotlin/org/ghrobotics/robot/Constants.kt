/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot

import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Rotation2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.units.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Constants {

    // CTRE TIMEOUT
    const val kCTRETimeout = 10

    // MOTOR IDS
    const val kLeftMasterId = 1
    const val kLeftSlaveId1 = 2

    const val kRightMasterId = 3
    const val kRightSlaveId1 = 4

    const val kElevatorMasterId = 5
    const val kElevatorSlaveId  = 6

    const val kIntakeMasterId = 7
    const val kIntakeSlaveId  = 9

    const val kArmId = 8

    const val kWinchMasterId = 10
    const val kWinchSlaveId  = 59

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

    // ROBOT DIMENSIONS
    const val kRobotWidth   = 27.0 / 12.0
    const val kRobotLength  = 33.0 / 12.0
    const val kIntakeLength = 16.0 / 12.0
    const val kBumperLength = 02.0 / 12.0

    // ROBOT POSES
    const val kRobotStartX = (kRobotLength / 2.0) + kBumperLength

    const val kExchangeZoneBottomY = 14.5
    const val kPortalZoneBottomY   = 27 - (29.69 / 12.0)

    const val kRobotSideStartY   = kPortalZoneBottomY   - (kRobotWidth / 2.0) - kBumperLength
    const val kRobotCenterStartY = kExchangeZoneBottomY - (kRobotWidth / 2.0) - kBumperLength

    // MECHANISM TRANSFORMATIONS
    val kCenterToIntake      = Pose2d(Translation2d(-(kRobotLength / 2.0) - kIntakeLength, 0.0), Rotation2d())
    val kCenterToFrontBumper = Pose2d(Translation2d(-(kRobotLength / 2.0) - kBumperLength, 0.0), Rotation2d())

    // DRIVE
    const val kDriveSensorUnitsPerRotation = 1440
    const val kWheelRadiusInches           = 3.0
    const val kTrackWidth                  = 27 / 12.0

    // ARM
    val kArmDownPosition = NativeUnits(-795)

    // DRIVE PID
    const val kPDrive = 2.0
    const val kVDrive = 0.656 // 1023 units per STU (velocity)
    const val kADrive = 0.030 // 1023 units per STU (acceleration)
    const val kSDrive = 0.050 // %

    const val kDriveBeta = 0.30
    const val kDriveZeta = 0.85

    // ELEVATOR PID
    const val kPElevator = 0.3
    const val kVElevator = 0.395 // 1023 units per STU (velocity)

    // ARM PID
    const val kPArm = 4.5
    const val kVArm = 16.78 + 0.9  // 1023 units per STU (velocity)

    // CLIMBER PID
    const val kPClimber = 2.0

    // LIMITS
    val kElevatorSoftLimitFwd = NativeUnits(22500)

    // TOLERANCE
    val kElevatorClosedLpTolerance = Inches(0.25)
    val kArmClosedLpTolerance      = Inches(0.0)
    val kClimberClosedLpTolerance  = NativeUnits(1000)

    // MOTION MAGIC
    val kElevatorMotionMagicVelocity     = InchesPerSecond(72.0, preferences { radius = 1.25 / 2.0 })
    val kElevatorMotionMagicAcceleration = InchesPerSecond(90.0, preferences { radius = 1.25 / 2.0 }).STU

    val kArmMotionMagicVelocity           = NativeUnitsPer100Ms(1000000)
    const val kArmMotionMagicAcceleration = 400

    val kClimberMotionMagicVelocity           = NativeUnitsPer100Ms(1000000)
    const val kClimberMotionMagicAcceleration = 12000
}

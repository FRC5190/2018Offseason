/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.geometry.Translation2d

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Constants {

    const val kLeftMasterId = 1
    const val kLeftSlaveId1 = 2
    const val kLeftSlaveId2 = 3

    const val kRightMasterId = 4
    const val kRightSlaveId1 = 5
    const val kRightSlaveId2 = 6

    const val kElevatorMasterId = 5
    const val kElevatorSlaveId  = 6

    const val kIntakeMasterId = 7
    const val kIntakeSlaveId  = 9

    const val kArmId = 8

    const val kWinchMasterId = 10
    const val kWinchSlaveId  = 59

    const val kRobotWidth   = 27.0 / 12.0
    const val kRobotLength  = 33.0 / 12.0
    const val kIntakeLength = 16.0 / 12.0
    const val kBumperLength = 02.0 / 12.0

    const val kRobotStartX = (kRobotLength / 2.0) + kBumperLength

    const val kExchangeZoneBottomY = 14.5
    const val kPortalZoneBottomY   = 27 - (29.69 / 12.0)

    const val kRobotSideStartY   = kPortalZoneBottomY   - (kRobotWidth / 2.0) - kBumperLength
    const val kRobotCenterStartY = kExchangeZoneBottomY - (kRobotWidth / 2.0) - kBumperLength

    val kCenterToIntake      = Pose2d(Translation2d(-(kRobotLength / 2.0) - kIntakeLength, 0.0), Rotation2d())
    val kCenterToFrontBumper = Pose2d(Translation2d(-(kRobotLength / 2.0) - kBumperLength, 0.0), Rotation2d())

    const val kDriveSensorUnitsPerRotation = 1440
    const val kWheelRadiusInches           = 3.0
    const val kTrackWidth                  = 25 / 12.0

    const val kPLeftDriveVelocity = 0.08
    const val kILeftDriveVelocity = 0.01
    const val kVLeftDriveVelocity = 0.05
    const val kSLeftDriveVelocity = 0.10

    const val kPRightDriveVelocity = 0.08
    const val kIRightDriveVelocity = 0.01
    const val kVRightDriveVelocity = 0.05
    const val kSRightDriveVelocity = 0.10

}

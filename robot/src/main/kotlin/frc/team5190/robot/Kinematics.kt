package frc.team5190.robot

import frc.team5190.lib.control.RobotVelocities
import frc.team5190.lib.control.WheelVelocities

// Object that contains kinematic equations
object Kinematics {

    // Converts linear and angular velocity into Feet Per Second values for the Talon SRX.
    fun inverseKinematics(vel: RobotVelocities): WheelVelocities {
        val v = vel.v
        val w = vel.w

        val leftRadS = ((2 * v) - (DriveConstants.TRACK_WIDTH * w)) / (DriveConstants.WHEEL_RADIUS / 6.0)
        val rightRadS = ((2 * v) + (DriveConstants.TRACK_WIDTH * w)) / (DriveConstants.WHEEL_RADIUS / 6.0)

        fun convertToFPS(value: Double) = value * (DriveConstants.WHEEL_RADIUS / 12.0)
        return WheelVelocities(convertToFPS(leftRadS), convertToFPS(rightRadS))
    }
}

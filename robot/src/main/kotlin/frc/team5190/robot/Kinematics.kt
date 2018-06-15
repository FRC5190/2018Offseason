@file:Suppress("LocalVariableName")

package frc.team5190.robot

// Object that contains kinematic equations
object Kinematics {

    // Converts linear and angular velocity into Feet Per Second values for the Talon SRX.
    fun inverseKinematics(vel: Pair<Double, Double>): Pair<Double, Double> {
        val v = vel.first
        val ω = vel.second

        val leftRadS = ((2 * v) - (DriveConstants.TRACK_WIDTH * ω)) / (DriveConstants.WHEEL_RADIUS / 6.0)
        val rightRadS = ((2 * v) + (DriveConstants.TRACK_WIDTH * ω)) / (DriveConstants.WHEEL_RADIUS / 6.0)

        fun convertToFPS(value: Double) = value * (DriveConstants.WHEEL_RADIUS / 12.0)
        return convertToFPS(leftRadS) to convertToFPS(rightRadS)
    }
}
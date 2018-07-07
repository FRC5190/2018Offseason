/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */

@file:Suppress("unused")

package frc.team5190.lib.geometry

import frc.team5190.lib.extensions.Vector2d
import frc.team5190.lib.extensions.atan2
import frc.team5190.lib.extensions.epsilonEquals
import java.lang.Math.abs
import kotlin.math.hypot

data class Twist2d(var dx: Double = 0.0, var dy: Double = 0.0, var dtheta: Double = 0.0) {


    val norm: Double
        get() {
            return if (dy == 0.0) {
                abs(dx)
            } else hypot(dx, dy)
        }

    val curvature: Double
        get() {
            return if (dtheta epsilonEquals 0.0 && norm epsilonEquals 0.0) {
                0.0
            } else {
                dtheta / norm
            }
        }

    fun scaled(scale: Double) = Twist2d(dx * scale, dy * scale, dtheta * scale)


    companion object {
        fun fromPose(pose: Pose2d): Twist2d {
            val dtheta = pose.rotation.radians
            val halfdtheta = dtheta / 2.0

            val cosMinusOne = pose.rotation.cos - 1.0

            val halfThetaByTanHalfTheta: Double

            halfThetaByTanHalfTheta = if (cosMinusOne epsilonEquals 0.0) {
                1.0 - 1.0 / 12.0 * dtheta * dtheta
            } else {
                (halfdtheta * pose.rotation.sin / cosMinusOne)
            }

            val translation = pose.translation.rotateBy(Rotation2d(halfThetaByTanHalfTheta, -halfdtheta))
            return Twist2d(translation.x, translation.y, dtheta)
        }
    }

    constructor(linear: Vector2d, angular: Vector2d) : this(linear.x, linear.y, angular.atan2)
    constructor(linear: Translation2d, angular: Rotation2d) : this(linear.x, linear.y, angular.radians)
}

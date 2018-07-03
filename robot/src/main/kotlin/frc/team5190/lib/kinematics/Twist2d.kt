@file:Suppress("unused")

package frc.team5190.lib.kinematics

import frc.team5190.lib.extensions.Vector2d
import frc.team5190.lib.extensions.atan2

data class Twist2d(var dx: Double, var dy: Double, var dtheta: Double) {
    constructor(linear: Vector2d, angular: Vector2d) : this(linear.x, linear.y, angular.atan2)
    constructor(linear: Translation2d, angular: Rotation2d) : this(linear.x, linear.y, angular.radians)
}

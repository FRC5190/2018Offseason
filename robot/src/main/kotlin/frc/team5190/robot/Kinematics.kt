package frc.team5190.robot

import frc.team5190.lib.extensions.epsilonEquals
import frc.team5190.lib.geometry.Twist2d
import frc.team5190.lib.motion.kinematics.DriveVelocity


object Kinematics {

    fun forwardKinematics(leftDelta: Double, rightDelta: Double, rotationDelta: Double): Twist2d {
        val dx = (leftDelta + rightDelta) / 2.0
        return Twist2d(dx = dx, dy = 0.0, dtheta = rotationDelta)
    }

    fun inverseKinematics(velocity: Twist2d): DriveVelocity {
        if (velocity.dtheta epsilonEquals 0.0) {
            return DriveVelocity(left = velocity.dx, right = velocity.dx)
        }

        val deltaV = Constants.TRACK_WIDTH * velocity.dtheta / 2
        return DriveVelocity(left = velocity.dx - deltaV, right = velocity.dx + deltaV)
    }
}

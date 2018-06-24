package frc.team5190.lib.control

import frc.team5190.lib.cos
import frc.team5190.lib.epsilonEquals
import frc.team5190.lib.math.EPSILON
import frc.team5190.lib.math.FrameOfReference
import frc.team5190.lib.math.Pose2D
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// https://www.dis.uniroma1.it/~labrob/pub/papers/Ramsete01.pdf
// Equation 6.11

class PostureStabilizer(targetPos: Pose2D) {

    private val targetFOR = FrameOfReference(targetPos.positionVector, targetPos.angle)

    var isFinished = false
        private set

    fun getRobotVelocity(robotPose: Pose2D): RobotVelocities {
        // Get position relative to target
        val relativePos = robotPose.convertTo(targetFOR)
        val velocities = getLinAndAngVelocities(relativePos)

        if (relativePos.positionVector.norm < 1E-2) {
            isFinished = true
            return RobotVelocities(0.0, 0.0)
        }

        System.out.printf("V: %.3f, A: %.3f, Polar X: %.3f, Polar Y: %.3f",
                velocities.v, velocities.w, relativePos.positionVector.x, relativePos.positionVector.y)

        return velocities
    }

    companion object {
        // Constants
        private const val k1 = 1.0
        private const val k2 = 0.3
        private const val k3 = 0.3

        // Returns linear and angular velocity
        fun getLinAndAngVelocities(relativePose: Pose2D): RobotVelocities {
            val p = relativePose.positionVector.norm
            val gamma = (atan2(relativePose.positionVector.y, relativePose.positionVector.x) - relativePose.angle + PI).let {
                if (it epsilonEquals 0.0) EPSILON else it
            }
            val delta = gamma + relativePose.angle

            val v = (k1 * p) cos gamma
            val w = (k2 * gamma) +
                    (k1 * ((sin(gamma) * cos(gamma)) / gamma) * (gamma + (k3 * delta)))

            return RobotVelocities(v, w)
        }
    }
}
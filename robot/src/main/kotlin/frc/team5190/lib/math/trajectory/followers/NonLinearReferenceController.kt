/*
 * FRC Team 5190
 * Green Hope Falcons
 */

/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.math.trajectory.followers

import frc.team5190.lib.extensions.cos
import frc.team5190.lib.extensions.epsilonEquals
import frc.team5190.lib.extensions.sin
import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Pose2dWithCurvature
import frc.team5190.lib.math.geometry.Twist2d
import frc.team5190.lib.math.trajectory.Trajectory
import frc.team5190.lib.math.trajectory.TrajectoryIterator
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.view.TimedView
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

// https://www.dis.uniroma1.it/~labrob/pub/papers/Ramsete01.pdf
// Equation 5.12

class NonLinearReferenceController(trajectory: Trajectory<TimedState<Pose2dWithCurvature>>) : TrajectoryFollower {


    // Trajectory S
    private val iterator = TrajectoryIterator(TimedView(trajectory))

    override var point = iterator.preview(0.0)

    override val pose
        get() = point.state.state.pose

    override val isFinished
        get() = iterator.isDone


    // Loops
    private var lastCallTime = -1L
    private var dt = -1.0

    // Returns desired linear and angular cruiseVelocity of the robot
    override fun getSteering(pose: Pose2d): Twist2d {
        val time = System.currentTimeMillis()
        if (lastCallTime < 0) lastCallTime = time

        dt = (time - lastCallTime).toDouble()

        return calculateTwist(
                xError = this.pose.translation.x - pose.translation.x,
                yError = this.pose.translation.y - pose.translation.y,
                thetaError = (this.pose.rotation - pose.rotation).radians,
                pathV = point.state.velocity,
                pathW = (iterator.preview(dt).state.state.rotation - this.pose.rotation).radians / dt,
                theta = pose.rotation.radians
        ).also { point = iterator.advance(dt) }
    }

    companion object {
        // Constants
        private const val kB = 1.0
        private const val kZeta = 0.7
        private const val kMaxSafeLinearVelocity = 12.0
        private const val kMaxSafeAngularVelocity = PI

        fun calculateTwist(xError: Double,
                           yError: Double,
                           thetaError: Double,
                           pathV: Double,
                           pathW: Double,
                           theta: Double): Twist2d {
            return Twist2d(
                    dx = calculateLinearVelocity(xError, yError, thetaError, pathV, pathW, theta)
                            .coerceIn(-kMaxSafeLinearVelocity, kMaxSafeLinearVelocity),
                    dy = 0.0,
                    dtheta = calculateAngularVelocity(xError, yError, thetaError, pathV, pathW, theta)
                            .coerceIn(-kMaxSafeAngularVelocity, kMaxSafeAngularVelocity))
        }

        private fun calculateLinearVelocity(xError: Double,
                                            yError: Double,
                                            thetaError: Double,
                                            pathV: Double,
                                            pathW: Double,
                                            theta: Double): Double {
            return (pathV cos thetaError) + (gainFunc(pathV, pathW) * ((xError cos theta) + (yError sin theta)))
        }

        private fun calculateAngularVelocity(xError: Double,
                                             yError: Double,
                                             thetaError: Double,
                                             pathV: Double,
                                             pathW: Double,
                                             theta: Double): Double {
            return pathW + (kB * pathV * safeFunc(thetaError) * ((yError cos theta) - (xError sin theta))) +
                    (gainFunc(pathV, pathW) * thetaError)
        }

        private fun gainFunc(v: Double, w: Double) = 2 * kZeta * sqrt((w * w) + ((kB) * (v * v)))
        private fun safeFunc(theta: Double): Double {
            return if (theta epsilonEquals 0.0) 1.0 - 1.0 / 6.0 * theta * theta
            else sin(theta) / theta
        }
    }
}
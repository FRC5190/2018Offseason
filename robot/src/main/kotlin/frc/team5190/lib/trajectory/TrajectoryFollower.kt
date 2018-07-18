/*
 * FRC Team 5190
 * Green Hope Falcons
 */


package frc.team5190.lib.trajectory

import frc.team5190.lib.extensions.cos
import frc.team5190.lib.extensions.epsilonEquals
import frc.team5190.lib.extensions.sin
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Pose2dWithCurvature
import frc.team5190.lib.geometry.Twist2d
import frc.team5190.lib.trajectory.timing.TimedState
import kotlin.math.sin
import kotlin.math.sqrt


// https://www.dis.uniroma1.it/~labrob/pub/papers/Ramsete01.pdf
// Equation 5.12

class TrajectoryFollower(trajectory: Trajectory<TimedState<Pose2dWithCurvature>>,
                         val dt: Double = 0.02) {

    private val trajectoryIterator = TrajectoryIterator(trajectory.indexView)


    var currentPoint = trajectoryIterator.preview(0.0)

    val currentPointPose
        get() = currentPoint.state.state.pose

    val isFinished
        get() = trajectoryIterator.isDone


    // Returns desired linear and angular cruiseVelocity of the robot
    fun getRobotVelocity(pose: Pose2d): Twist2d {
        currentPoint = trajectoryIterator.advance(dt)

        val xError = currentPointPose.translation.x - pose.translation.x
        val yError = currentPointPose.translation.y - pose.translation.y
        val thetaError = currentPointPose.rotation - pose.rotation

        val sv = currentPoint.state.velocity
        val sw = (trajectoryIterator.preview(dt).state.state.rotation - currentPointPose.rotation).radians / dt


        val v = calculateLinearVelocity(xError, yError, thetaError.radians, sv, sw, pose.rotation.radians)
        val w = calculateAngularVelocity(xError, yError, thetaError.radians, sv, sw, pose.rotation.radians)

        return Twist2d(dx = v, dy = 0.0, dtheta = w)
    }

    companion object {
        // Constants
        private const val b = 0.65
        private const val zeta = 0.175

        fun calculateLinearVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, theta: Double) =
                (pathV cos thetaError) +
                        (gainFunc(pathV, pathW) * ((xError cos theta) + (yError sin theta)))


        fun calculateAngularVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, theta: Double) =
                pathW +
                        (b * pathV * safeFunc(thetaError) * ((yError cos theta) - (xError sin theta))) +
                        (gainFunc(pathV, pathW) * thetaError)

        private fun gainFunc(v: Double, w: Double): Double {
            return 2 * zeta * sqrt((w * w) + ((b) * (v * v)))
        }

        private fun safeFunc(theta: Double) = if (theta epsilonEquals 0.0) {
            1.0 - 1.0 / 6.0 * theta * theta
        } else {
            sin(theta) / theta
        }
    }
}
package frc.team5190.lib.control

import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PathFollower(private val trajectory: Trajectory) {

    private var currentSegmentIndex = 0

    val isFinished
        get() = currentSegmentIndex == trajectory.segments.size - 1

    fun getMotorOutput(pose: Vector2D, gyroAngleRadians: Double): Pair<Double, Double> {

        val currentSegment = trajectory.segments[currentSegmentIndex]

        val xError = currentSegment.x - pose.x
        val yError = currentSegment.y - pose.y
        val thetaError = currentSegment.heading - gyroAngleRadians

        val sv = currentSegment.velocity
        val sw = if (currentSegmentIndex == trajectory.segments.size - 1) {
            0.0
        } else {
            (trajectory.segments[currentSegmentIndex + 1].heading - currentSegment.heading) / currentSegment.dt
        }

        val v = calculateLinearVelocity(xError, yError, thetaError, sv, sw)
        val a = calculateAngularVelocity(xError, yError, thetaError, sv, sw)

        currentSegmentIndex++

        return v to a
    }

    companion object {
        private const val k1 = 1
        private const val k2 = 0.1

        private fun calculateLinearVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double): Double {
            return (pathV * cos(thetaError)) +
                    (gainFunc(pathV, pathW) * (cos(xError) + sin(yError)))
        }

        private fun calculateAngularVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double): Double {
            return pathW +
                    (k1 * pathV * (sin(thetaError) / thetaError) * (cos(yError) - sin(xError))) +
                    (gainFunc(pathV, pathW) * thetaError)
        }

        private fun gainFunc(v: Double, w: Double): Double {
            return 2 * k2 * sqrt((w * w) + ((k1) * (v * v)))
        }
    }
}
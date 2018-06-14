package frc.team5190.lib.control

import frc.team5190.lib.epsilonEquals
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import kotlin.math.*

class PathFollower(private val trajectory: Trajectory) {

    private var currentSegmentIndex = 0
    var currentSegment = trajectory.segments[0]

    val isFinished
        get() = currentSegmentIndex == trajectory.segments.size - 1

    fun getMotorOutput(pose: Vector2D, gyroAngleRadians: Double): Pair<Double, Double> {

        currentSegmentIndex = getCurrentSegmentIndex(pose, currentSegmentIndex)

        if (currentSegmentIndex >= trajectory.segments.size) return 0.0 to 0.0

        currentSegment = trajectory.segments[currentSegmentIndex]

        val xError = currentSegment.x - pose.x
        val yError = currentSegment.y - pose.y
        val thetaError = currentSegment.heading - gyroAngleRadians

        val sv = currentSegment.velocity
        val sw = if (currentSegmentIndex == trajectory.segments.size - 1) {
            0.0
        } else {
            (trajectory.segments[currentSegmentIndex + 1].heading - currentSegment.heading) / currentSegment.dt
        }

        val v = calculateLinearVelocity(xError, yError, thetaError, sv, sw, gyroAngleRadians).coerceAtMost(10.0)
        val a = if (thetaError epsilonEquals 0.0) 0.0 else calculateAngularVelocity(xError, yError, thetaError, sv, sw, gyroAngleRadians)

        currentSegmentIndex++

        return v to a
    }

    // Returns the index of the current segment
    private fun getCurrentSegmentIndex(robotPosition: Vector2D, estimatedIndex: Int): Int {
        (0 until trajectory.segments.size - 1 - estimatedIndex).forEach { index ->

            // Check indices at and after estimate
            if (isRobotOnPerpendicular(robotPosition, trajectory.segments[estimatedIndex + index])) {
                return estimatedIndex + index
            }

            // Check indices before estimate if they exist
            if (estimatedIndex - index >= 0) {
                if (isRobotOnPerpendicular(robotPosition, trajectory.segments[estimatedIndex - index])) {
                    return estimatedIndex - index
                }
            }
        }
        return estimatedIndex
    }

    // Returns if the robot is perpendicular to a segment
    private fun isRobotOnPerpendicular(robotPosition: Vector2D, segment: Trajectory.Segment): Boolean {

        if (robotPosition.x == segment.x) return true

        val perpendicularSlope = if (tan(segment.heading) != Double.NaN) -1 / tan(segment.heading) else 0.0
        return (((robotPosition.y - segment.y) / (robotPosition.x - segment.x)) - perpendicularSlope).absoluteValue < 0.0002
    }


    companion object {
        private const val k1 = 0.3
        private const val k2 = 0.9

        private fun calculateLinearVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, robotAngle: Double): Double {
            return (pathV * cos(thetaError)) +
                    (gainFunc(pathV, pathW) * ((cos(robotAngle) * (xError)) + (sin(robotAngle) * (yError))))
        }

        private fun calculateAngularVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, robotAngle: Double): Double {
            return pathW +
                    (k1 * pathV * (sin(thetaError) / thetaError) * ((cos(robotAngle) * (yError)) - (sin(robotAngle) * (xError)))) +
                    (gainFunc(pathV, pathW) * thetaError)
        }

        private fun gainFunc(v: Double, w: Double): Double {
            return 2 * k2 * sqrt((w * w) + ((k1) * (v * v)))
        }



        @JvmStatic
        fun main(args: Array<String>) {
//            println(calculateLinearVelocity(0.4, 0.002, Math.toRadians(0)))
        }
    }
}
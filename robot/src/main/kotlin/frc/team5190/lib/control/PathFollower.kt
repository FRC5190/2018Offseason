package frc.team5190.lib.control

import frc.team5190.lib.cos
import frc.team5190.lib.enforceBounds
import frc.team5190.lib.epsilonEquals
import frc.team5190.lib.math.EPSILON
import frc.team5190.lib.sin
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

/*
Class that follows a path given a trajectory while closing the loop on
X, Y, and Theta error
https://www.dis.uniroma1.it/~labrob/pub/papers/Ramsete01.pdf
*/

class PathFollower(private val trajectory: Trajectory) {

    // Stores the current segment index
    private var currentSegmentIndex = 0

    // Stores the current segment
    var currentSegment: Trajectory.Segment = trajectory.segments[0]
        private set

    val isFinished
        get() = currentSegmentIndex == trajectory.segments.size - 1


    // Returns desired linear and angular velocity of the robot
    fun getLinAndAngVelocities(pose: Vector2D, theta: Double): RobotVelocities {

        // Update the current segment
        currentSegment = trajectory.segments[currentSegmentIndex]
        if (currentSegmentIndex >= trajectory.segments.size) return RobotVelocities(0.0, 0.0)

        // Calculate X and Y error
        val xError = currentSegment.x - pose.x
        val yError = currentSegment.y - pose.y

        // Calculate Theta Error
        var thetaError = (currentSegment.heading - theta).enforceBounds()
        thetaError = thetaError.let { if (it epsilonEquals 0.0) EPSILON else it }

        // Linear Velocity of the Segment
        val sv = currentSegment.velocity

        // Angular Velocity of the Segment
        val sw = if (currentSegmentIndex == trajectory.segments.size - 1) {
            0.0
        } else {
            (trajectory.segments[currentSegmentIndex + 1].heading - currentSegment.heading) / currentSegment.dt
        }

        // Calculate Linear and Angular Velocity based on errors
        val v = calculateLinearVelocity(xError, yError, thetaError, sv, sw, theta)
        val w = calculateAngularVelocity(xError, yError, thetaError, sv, sw, theta)

        // Increment segment index
        currentSegmentIndex++

        // Logging
        println("V: $v," +
                "A: $w," +
                "X Error: $xError," +
                "Y Error: $yError," +
                "Theta Error: $thetaError")

        return RobotVelocities(v, w)
    }

    companion object {
        // Constants
        private const val b = 0.25
        private const val zeta = 0.3

        // Returns linear velocity
        fun calculateLinearVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, theta: Double): Double {
            return (pathV cos thetaError) +
                    (gainFunc(pathV, pathW) * ((xError cos theta) + (yError sin theta)))
                            .coerceAtMost(10.0) // Limit linear velocity to 10 feet per second

        }

        // Returns angular velocity
        fun calculateAngularVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, theta: Double): Double {
            return pathW +
                    (b * pathV * (sin(thetaError) / thetaError) * ((yError cos theta) - (xError sin theta))) +
                    (gainFunc(pathV, pathW) * thetaError)
                            .coerceAtMost(PI) // Limit angular velocity to PI radians per second
        }

        // Gain function
        private fun gainFunc(v: Double, w: Double): Double {
            return 2 * zeta * sqrt((w * w) + ((b) * (v * v)))
        }
    }
}
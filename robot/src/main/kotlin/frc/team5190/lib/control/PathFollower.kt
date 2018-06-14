package frc.team5190.lib.control

import frc.team5190.lib.epsilonEquals
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.jetbrains.annotations.TestOnly
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


// Class that follows a path given a trajectory while closing the loop on
// X, Y, and Theta error

// https://www.dis.uniroma1.it/~labrob/pub/papers/Ramsete01.pdf

class PathFollower(private val trajectory: Trajectory) {

    // Stores the current segment index
    private var currentSegmentIndex = 0

    // Stores the current segment
    var currentSegment: Trajectory.Segment = trajectory.segments[0]
        private set

    val isFinished
        get() = currentSegmentIndex == trajectory.segments.size - 1


    // Returns desired linear and angular velocity of the robot
    fun getLinAndAngVelocities(pose: Vector2D, gyroAngle: Double): Pair<Double, Double> {

        // Update the current segment
        currentSegment = trajectory.segments[currentSegmentIndex]
        if (currentSegmentIndex >= trajectory.segments.size) return 0.0 to 0.0

        // Calculate X and Y error
        val xError = currentSegment.x - pose.x
        val yError = currentSegment.y - pose.y

        // Calculate Theta Error
        var thetaError = Math.toRadians(Pathfinder.boundHalfDegrees(Math.toDegrees(currentSegment.heading) - gyroAngle))
        if (thetaError epsilonEquals 0.0) thetaError = 1E-4

        // Linear Velocity of the Segment
        val sv = currentSegment.velocity

        // Angular Velocity of the Segment
        val sw = if (currentSegmentIndex == trajectory.segments.size - 1) {
            0.0
        } else {
            (trajectory.segments[currentSegmentIndex + 1].heading - currentSegment.heading) / currentSegment.dt
        }

        // Calculate Linear and Angular Velocity based on errors
        val v = calculateLinearVelocity(xError, yError, thetaError, sv, sw, Math.toRadians(gyroAngle))
        val a = calculateAngularVelocity(xError, yError, thetaError, sv, sw, Math.toRadians(gyroAngle))

        // Increment segment index
        currentSegmentIndex++

        // Logging
        println("V: $v," +
                "A: $a," +
                "X Error: $xError," +
                "Y Error: $yError," +
                "Theta Error: $thetaError")

        return v to a
    }

    companion object {
        // Constants
        private const val k1 = 0.25
        private const val k2 = 0.3

        // Returns linear velocity
        private fun calculateLinearVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, robotAngle: Double): Double {
            return (pathV * cos(thetaError)) +
                    (gainFunc(pathV, pathW) * ((cos(robotAngle) * (xError)) + (sin(robotAngle) * (yError)))).coerceAtMost(10.0)
        }

        // Returns angular velocity
        private fun calculateAngularVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, robotAngle: Double): Double {
            return pathW +
                    (k1 * pathV * (sin(thetaError) / thetaError) * ((cos(robotAngle) * (yError)) - (sin(robotAngle) * (xError)))) +
                    (gainFunc(pathV, pathW) * thetaError).coerceAtMost(PI)
        }

        // Gain function
        private fun gainFunc(v: Double, w: Double): Double {
            return 2 * k2 * sqrt((w * w) + ((k1) * (v * v)))
        }


        @TestOnly
        @JvmStatic
        fun main(args: Array<String>) {
            println(calculateLinearVelocity(0.0, 0.0, -0.034, 0.029, -0.02, 0.0))
            println(calculateAngularVelocity(0.0, 0.0, -0.034, 0.029, -0.02, 0.0))
        }
    }
}
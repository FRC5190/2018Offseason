@file:Suppress("ObjectPropertyName", "LocalVariableName")

package frc.team5190.lib.control

import frc.team5190.lib.enforceBounds
import frc.team5190.lib.cos
import frc.team5190.lib.epsilonEquals
import frc.team5190.lib.math.ε
import frc.team5190.lib.sin
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.jetbrains.annotations.TestOnly
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
    fun getLinAndAngVelocities(pose: Vector2D, θ: Double): Pair<Double, Double> {

        // Update the current segment
        currentSegment = trajectory.segments[currentSegmentIndex]
        if (currentSegmentIndex >= trajectory.segments.size) return 0.0 to 0.0

        // Calculate X and Y error
        val xError = currentSegment.x - pose.x
        val yError = currentSegment.y - pose.y

        // Calculate Theta Error
        var thetaError = (Math.toDegrees(currentSegment.heading) - θ).enforceBounds()
        thetaError = thetaError.let { if (it epsilonEquals 0.0) ε else it }

        // Linear Velocity of the Segment
        val sv = currentSegment.velocity

        // Angular Velocity of the Segment
        val sω = if (currentSegmentIndex == trajectory.segments.size - 1) {
            0.0
        } else {
            (trajectory.segments[currentSegmentIndex + 1].heading - currentSegment.heading) / currentSegment.dt
        }

        // Calculate Linear and Angular Velocity based on errors
        val v = calculateLinearVelocity(xError, yError, thetaError, sv, sω, θ)
        val ω = calculateAngularVelocity(xError, yError, thetaError, sv, sω, θ)

        // Increment segment index
        currentSegmentIndex++

        // Logging
        println("V: $v," +
                "A: $ω," +
                "X Error: $xError," +
                "Y Error: $yError," +
                "Theta Error: $thetaError")

        return v to ω
    }

    companion object {
        // Constants
        private const val b = 0.25
        private const val ζ = 0.3

        // Returns linear velocity
        private fun calculateLinearVelocity(xError: Double, yError: Double, θerror: Double, pathV: Double, pathW: Double, θ: Double): Double {
            return (pathV cos θerror) +
                    (gainFunc(pathV, pathW) * ((xError cos θ) + (yError sin θ)))
                            .coerceAtMost(10.0) // Limit linear velocity to 10 feet per second

        }

        // Returns angular velocity
        private fun calculateAngularVelocity(xError: Double, yError: Double, θerror: Double, pathV: Double, pathW: Double, θ: Double): Double {
            return pathW +
                    (b * pathV * (sin(θerror) / θerror) * ((yError cos θ) - (xError sin θ))) +
                    (gainFunc(pathV, pathW) * θerror)
                            .coerceAtMost(PI) // Limit angular velocity to PI radians per second
        }

        // Gain function
        private fun gainFunc(v: Double, w: Double): Double {
            return 2 * ζ * sqrt((w * w) + ((b) * (v * v)))
        }
        @TestOnly
        @JvmStatic
        fun main(args: Array<String>) {
            println(calculateLinearVelocity(0.0, 0.0, 1E-9, 0.029, -0.02, 0.0))
            println(calculateAngularVelocity(0.0, 1.0, 1E-9, 0.029, -0.02, 0.0))
        }
    }
}
package frc.team5190.lib.control

import frc.team5190.lib.epsilonEquals
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.jetbrains.annotations.TestOnly
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PathFollower(private val trajectory: Trajectory) {

    private var currentSegmentIndex = 0
    var currentSegment: Trajectory.Segment = trajectory.segments[0]

    val isFinished
        get() = currentSegmentIndex == trajectory.segments.size - 1

    fun getMotorOutput(pose: Vector2D, gyroAngleRadians: Double): Pair<Double, Double> {
        currentSegment = trajectory.segments[currentSegmentIndex]

        if (currentSegmentIndex >= trajectory.segments.size) return 0.0 to 0.0

        val xError = currentSegment.x - pose.x
        val yError = currentSegment.y - pose.y
        var thetaError = Math.toRadians(Pathfinder.boundHalfDegrees(Math.toDegrees(currentSegment.heading) - Math.toDegrees(gyroAngleRadians)))

        if (thetaError epsilonEquals 0.0) thetaError = 1E-4

        val sv = currentSegment.velocity
        val sw = if (currentSegmentIndex == trajectory.segments.size - 1) {
            0.0
        } else {
            (trajectory.segments[currentSegmentIndex + 1].heading - currentSegment.heading) / currentSegment.dt
        }

        val v = calculateLinearVelocity(xError, yError, thetaError, sv, sw, gyroAngleRadians).coerceAtMost(10.0)
        val a = calculateAngularVelocity(xError, yError, thetaError, sv, sw, gyroAngleRadians)

        currentSegmentIndex++

        println("V: $v," +
                "A: $a," +
                "X Error: $xError," +
                "Y Error: $yError," +
                "Theta Error: $thetaError")

        return v to a
    }

    companion object {
        private const val k1 = 0.25
        private const val k2 = 0.3

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

        @TestOnly
        @JvmStatic
        fun main(args: Array<String>) {
            println(calculateLinearVelocity(0.0, 0.0, -0.034, 0.029, -0.02, 0.0))
            println(calculateAngularVelocity(0.0, 0.0, -0.034, 0.029, -0.02, 0.0))
        }
    }
}
package frc.team5190.lib.control

import frc.team5190.lib.units.FeetPerSecond
import frc.team5190.lib.units.Speed
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.apache.commons.math3.stat.regression.SimpleRegression
import kotlin.math.*

class PathFollower(val trajectory: Trajectory,
                   val reversed: Boolean) {

    // Current Segment
    var currentSegment: Trajectory.Segment = trajectory.segments[0]
        private set

    // Lookahead Segment
    var lookaheadSegment: Trajectory.Segment = trajectory.segments[0]
        private set

    // Proportional Feedback and Velocity Feed-Forward Constants
    var p = 0.0
    var v = 0.0
    var vIntercept = 0.0
    var a = 0.0

    // Proprtional Feedback Constant for Cross-Path Error Correction
    var pTurn = 0.0

    // Variable that stores the state of completion
    var isFinished = false
        private set

    // Estimation of the current segment index
    private var segmentIndexEstimation = 0


    // Compute lookahead value based on speed.
    // Feet Per Second to Feet
    private val lookaheadInterpolationData = arrayOf(0.0 to 1.0, 4.0 to 2.0, 8.0 to 4.0)
    private val lookaheadInterpolator = SimpleRegression()

    // Interpolate Data
    init {
        lookaheadInterpolationData.forEach { lookaheadInterpolator.addData(it.first, it.second) }
    }

    // Return motor output based on robot pose.
    fun getMotorOutput(robotPosition: Vector2D, robotAngle: Double, rawEncoderVelocities: Pair<Speed, Speed>): Pair<Double, Double> {

        // Make sure velocities are positive
        val velocities = rawEncoderVelocities.first.absoluteValue to rawEncoderVelocities.second.absoluteValue

        segmentIndexEstimation = getCurrentSegmentIndex(robotPosition, segmentIndexEstimation)
        currentSegment = trajectory[segmentIndexEstimation]

        // All points in the path have been exhausted
        if (segmentIndexEstimation >= trajectory.segments.size - 1) {
            isFinished = true
            return 0.0 to 0.0
        }

        // Get lookahead point based on speed
        val lookaheadDistance = lookaheadInterpolator.predict((velocities.first + velocities.second).FPS.value / 2.0)

        // Returns imaginary segment at the end of the path so weird end-behavor isn't present
        fun getImaginarySegment(): Trajectory.Segment {
            val lastSegment = trajectory.segments.last()
            val theta = lastSegment.heading
            val magnitude = lookaheadDistance - (lastSegment.position - currentSegment.position)
            val vector = Vector2D(magnitude * cos(theta), magnitude * sin(theta))
            return Trajectory.Segment(
                    lastSegment.dt,
                    lastSegment.x + vector.x,
                    lastSegment.y + vector.y,
                    lastSegment.position,
                    lastSegment.velocity,
                    lastSegment.acceleration,
                    lastSegment.jerk,
                    lastSegment.heading
            )
        }

        lookaheadSegment = trajectory.segments.copyOfRange(segmentIndexEstimation, trajectory.segments.size).find { segment ->
            return@find segment.position - currentSegment.position >= lookaheadDistance
        } ?: getImaginarySegment()

        val desiredPosition = Vector2D(lookaheadSegment.x, lookaheadSegment.y)

        // Positional error
        val positionDelta = robotPosition.negate().add(desiredPosition)
        val actualLookaheadDistance = robotPosition.distance(desiredPosition)

        // Use FF and FB to calculate output
        fun calculateOutput(targetSpeed: Speed, actualVelocity: Speed, acceleration: Double, reversed: Boolean): Double {
            val velocityError = targetSpeed - actualVelocity

            val feedForward = v * targetSpeed.FPS.value + vIntercept + a * acceleration
            val feedback = p * velocityError.FPS.value

            val output = feedForward + feedback
            return if (reversed) -1.0 else 1.0 * output
        }

        // Turn output is directly proportional to angle delta and lookahead distance
        val theta = -Pathfinder.boundHalfDegrees(Math.toDegrees(atan2(positionDelta.y, positionDelta.x)) - robotAngle)
        val turnOutput = pTurn * theta

        val leftOutput = calculateOutput(FeetPerSecond(trajectory[segmentIndexEstimation].velocity) + FeetPerSecond(turnOutput),
                velocities.first, trajectory[segmentIndexEstimation].acceleration, reversed)
        val rightOutput = calculateOutput(FeetPerSecond(trajectory[segmentIndexEstimation].velocity) - FeetPerSecond(turnOutput),
                velocities.second, trajectory[segmentIndexEstimation].acceleration, reversed)

        segmentIndexEstimation++

        println("Current Segment: $segmentIndexEstimation, Lookahead Dist: $actualLookaheadDistance, Lookahead Theta: $theta")
        return leftOutput to rightOutput
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
}


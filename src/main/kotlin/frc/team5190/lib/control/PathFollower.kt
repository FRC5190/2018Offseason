@file:Suppress("unused")

package frc.team5190.lib.control

import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.apache.commons.math3.stat.regression.SimpleRegression
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.tan

class PathFollower(val leftTrajectory: Trajectory, val rightTrajectory: Trajectory, val sourceTrajectory: Trajectory, val reversed: Boolean) {

    // PDVA Constants for path following
    var p = 0.0
    var d = 0.0
    var v = 0.0
    var vIntercept = 0.0
    var a = 0.0

    // P constant for turning
    var pTurn = 0.0

    // Track current state of path following
    var isFinished = false
        private set
    private var segmentIndexEstimation = 0


    // Error values
    private var leftVelocityLastError = 0.0
    private var rightVelocityLastError = 0.0

    // Compute lookahead value based on speed.
    // Feet per second to feet
    private val lookaheadInterpolationData = arrayOf(0.0 to 0.2, 4.0 to 0.7, 8.0 to 1.5)
    private val lookaheadInterpolator = SimpleRegression()

    init {
        lookaheadInterpolationData.forEach { lookaheadInterpolator.addData(it.first, it.second) }
    }

    fun getMotorOutput(robotPosition: Vector2D, robotAngle: Double, rawEncoderVelocities: Pair<Double, Double>): Pair<Double, Double> {

        val encoderVelocities = if (reversed) -rawEncoderVelocities.first to -rawEncoderVelocities.second
        else rawEncoderVelocities

        segmentIndexEstimation = getCurrentSegmentIndex(robotPosition, segmentIndexEstimation)

        // All points in the path have been exhausted
        if (segmentIndexEstimation >= sourceTrajectory.segments.size - 1) {
            isFinished = true
            return 0.0 to 0.0
        }

        // Left side velocity calculations
        val leftSegment = leftTrajectory.segments[segmentIndexEstimation]
        val leftVelocityError = leftSegment.velocity - encoderVelocities.first

        val leftFeedForward = v * leftSegment.velocity + a * leftSegment.acceleration + vIntercept
        val leftFeedback = p * leftVelocityError + d * ((leftVelocityError - leftVelocityLastError) / leftSegment.dt)

        var leftOutput = leftFeedForward + leftFeedback
        if (reversed) leftOutput *= -1

        leftVelocityLastError = leftVelocityError

        // Right side calculations
        val rightSegment = rightTrajectory.segments[segmentIndexEstimation]
        val rightVelocityError = rightSegment.position - encoderVelocities.second

        val rightFeedForward = v * rightSegment.velocity + a * rightSegment.acceleration + vIntercept
        val rightFeedback = p * rightVelocityError + d * ((rightVelocityError - rightVelocityLastError) / rightSegment.dt)

        var rightOutput = rightFeedForward + rightFeedback
        if (reversed) rightOutput *= -1

        rightVelocityLastError = rightVelocityError


        // Get lookahead point based on speed
        val lookaheadDistance = lookaheadInterpolator.predict((encoderVelocities.first + encoderVelocities.second) / 2.0)

        val lookaheadSegment = sourceTrajectory.segments.copyOfRange(segmentIndexEstimation, sourceTrajectory.segments.size).find { segment ->
            val vector = Vector2D(segment.x, segment.y)
            return@find robotPosition.distance(vector) >= lookaheadDistance
        } ?: sourceTrajectory.segments.last()

        val desiredPosition = Vector2D(lookaheadSegment.x, lookaheadSegment.y)

        // Positional error
        val positionDelta = robotPosition.negate().add(desiredPosition)
        val actualLookaheadDistance = robotPosition.distance(desiredPosition)

        // Turn output is directly proportional to angle delta and lookahead distance
        val theta = Pathfinder.boundHalfDegrees(Math.toDegrees(atan2(positionDelta.y, positionDelta.x)) - robotAngle)
        val turnOutput = pTurn * theta * actualLookaheadDistance

        segmentIndexEstimation++

        return leftOutput + turnOutput to rightOutput - turnOutput
    }

    private fun getCurrentSegmentIndex(robotPosition: Vector2D, estimatedIndex: Int): Int {
        (0 until sourceTrajectory.segments.size - 1 - estimatedIndex).forEach { index ->

            // Check indices at and after estimate
            if (isRobotOnPerpendicular(robotPosition, sourceTrajectory.segments[estimatedIndex + index])) {
                return estimatedIndex + index
            }

            // Check indices before estimate if they exist
            if (estimatedIndex - index >= 0) {
                if (isRobotOnPerpendicular(robotPosition, sourceTrajectory.segments[estimatedIndex - index])) {
                    return estimatedIndex - index
                }
            }
        }

        return estimatedIndex
    }

    private fun isRobotOnPerpendicular(robotPosition: Vector2D, segment: Trajectory.Segment): Boolean {

        if (robotPosition.x - segment.x == 0.0) return true

        val perpendicularSlope = if (tan(segment.heading) != Double.NaN) -1 / Math.tan(segment.heading) else 0.0
        return (((robotPosition.y - segment.y) / (robotPosition.x - segment.x)) - perpendicularSlope).absoluteValue < 0.2
    }
}

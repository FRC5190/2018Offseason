@file:Suppress("unused")

package frc.team5190.lib.control

import frc.team5190.lib.units.FeetPerSecond
import frc.team5190.lib.units.Speed
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.apache.commons.math3.stat.regression.SimpleRegression
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.tan

class PathFollower(val leftTrajectory: Trajectory,
                   val rightTrajectory: Trajectory,
                   val sourceTrajectory: Trajectory,
                   val reversed: Boolean) {

    // PVA Constants for path following
    var p = 0.0
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
    private var leftVelocityLastError: Speed = FeetPerSecond(0.0)
    private var rightVelocityLastError: Speed = FeetPerSecond(0.0)

    // Compute lookahead value based on speed.
    // FPS to FT
    private val lookaheadInterpolationData = arrayOf(0.0 to 1.0, 4.0 to 2.0, 8.0 to 2.0)
    private val lookaheadInterpolator = SimpleRegression()

    // Interpolate Data
    init {
        lookaheadInterpolationData.forEach { lookaheadInterpolator.addData(it.first, it.second) }
    }

    // Return motor output based on robot pose.
    fun getMotorOutput(robotPosition: Vector2D, robotAngle: Double, rawEncoderVelocities: Pair<Speed, Speed>): Pair<Double, Double> {

        val velocities = if (reversed) -rawEncoderVelocities.first to -rawEncoderVelocities.second
        else rawEncoderVelocities

        segmentIndexEstimation = getCurrentSegmentIndex(robotPosition, segmentIndexEstimation)

        // All points in the path have been exhausted
        if (segmentIndexEstimation >= sourceTrajectory.segments.size - 1) {
            isFinished = true
            return 0.0 to 0.0
        }

        // Use FF and FB to calculate base output
        fun calculateOutput(trajectory: Trajectory, actualVelocity: Speed, reversed: Boolean): Double {
            val segment = trajectory.segments[segmentIndexEstimation]
            val velocityError = FeetPerSecond(segment.velocity) - actualVelocity

            val feedForward = v * segment.velocity + a * segment.acceleration + vIntercept
            val feedback = p * velocityError.FPS.value

            val output = feedForward + feedback
            return if (reversed) -1.0 else 1.0 * output
        }

        // Assign base outputs
        val leftOutput = calculateOutput(leftTrajectory, velocities.first, reversed)
        val rightOutput = calculateOutput(rightTrajectory, velocities.second, reversed)


        // Get lookahead point based on speed
        val lookaheadDistance = lookaheadInterpolator.predict((velocities.first + velocities.second).FPS.value / 2.0)
        val segmentVector = Vector2D(sourceTrajectory[segmentIndexEstimation].x, sourceTrajectory[segmentIndexEstimation].y)

        val lookaheadSegment = sourceTrajectory.segments.copyOfRange(segmentIndexEstimation, sourceTrajectory.segments.size).find { segment ->
            val vector = Vector2D(segment.x, segment.y)
            return@find segmentVector.distance(vector) >= lookaheadDistance
        } ?: sourceTrajectory.segments.last()

        val desiredPosition = Vector2D(lookaheadSegment.x, lookaheadSegment.y)

        // Positional error
        val positionDelta = robotPosition.negate().add(desiredPosition)
        val actualLookaheadDistance = robotPosition.distance(desiredPosition)

        // Turn output is directly proportional to angle delta and lookahead distance
        val theta = -Pathfinder.boundHalfDegrees(Math.toDegrees(atan2(positionDelta.y, positionDelta.x)) - robotAngle)
        val turnOutput = pTurn * theta

        segmentIndexEstimation++

        println("Current Segment: $segmentIndexEstimation, Lookahead Dist: $actualLookaheadDistance, Lookahead Theta: $theta")
        return leftOutput + turnOutput to rightOutput - turnOutput
    }

    // Returns the index of the current segment
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

    // Returns if the robot is perpendicular to a segment
    private fun isRobotOnPerpendicular(robotPosition: Vector2D, segment: Trajectory.Segment): Boolean {

        if (robotPosition.x - segment.x == 0.0) return true

        val perpendicularSlope = if (tan(segment.heading) != Double.NaN) -1 / Math.tan(segment.heading) else 0.0
        return (((robotPosition.y - segment.y) / (robotPosition.x - segment.x)) - perpendicularSlope).absoluteValue < 0.002
    }
}


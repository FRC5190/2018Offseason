package frc.team5190.lib.control

import frc.team5190.lib.cos
import frc.team5190.lib.enforceBounds
import frc.team5190.lib.epsilonEquals
import frc.team5190.lib.math.EPSILON
import frc.team5190.lib.math.Pose2D
import frc.team5190.lib.sin
import frc.team5190.robot.drive.DriveSubsystem
import jaci.pathfinder.Trajectory
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
    fun getLinAndAngVelocities(pose: Pose2D): RobotVelocities {

        // Update the current segment
        if (currentSegmentIndex >= trajectory.segments.size) return RobotVelocities(0.0, 0.0)
        currentSegment = trajectory.segments[currentSegmentIndex]

        // Calculate X and Y error
        val xError = currentSegment.x - pose.vector.x
        val yError = currentSegment.y - pose.vector.y

        // Calculate Theta Error
        var thetaError = (currentSegment.heading - pose.yaw).enforceBounds()
        thetaError = thetaError.let { if (it epsilonEquals 0.0) EPSILON else it }

        // Linear Velocity of the Segment
        val sv = currentSegment.velocity

        // Angular Velocity of the Segment
        val sw = if (currentSegmentIndex == trajectory.segments.size - 1) {
            0.0
        } else {
            (trajectory.segments[currentSegmentIndex + 1].heading - currentSegment.heading).enforceBounds() / currentSegment.dt
        }

        // Calculate Linear and Angular Velocity based on errors
        val v = calculateLinearVelocity(xError, yError, thetaError, sv, sw, pose.yaw)
        val w = calculateAngularVelocity(xError, yError, thetaError, sv, sw, pose.yaw)

        // Increment segment index
        currentSegmentIndex++

        System.out.printf("[DEBUG] V: %2.3f, A: %2.3f, X Error: %2.3f, Y Error: %2.3f, Theta Error: %2.3f, Actual Speed: %2.3f %n",
                v, w, xError, yError, thetaError, (DriveSubsystem.leftVelocity + DriveSubsystem.rightVelocity).FPS.value / 2)

        return RobotVelocities(v, w)
    }

    companion object {
        // Constants
        private const val b = 0.65
        private const val zeta = 0.175

        // Returns linear velocity
        fun calculateLinearVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, theta: Double): Double {
            return ((pathV cos thetaError) +
                    (gainFunc(pathV, pathW) * ((xError cos theta) + (yError sin theta))))
                    .coerceIn(-10.0, 10.0) // Limit linear velocity to 10 feet per second

        }

        // Returns angular velocity
        fun calculateAngularVelocity(xError: Double, yError: Double, thetaError: Double, pathV: Double, pathW: Double, theta: Double): Double {
            return (pathW +
                    (b * pathV * (sin(thetaError) / thetaError) * ((yError cos theta) - (xError sin theta))) +
                    (gainFunc(pathV, pathW) * thetaError))
                    .coerceIn(-PI, PI) // Limit angular velocity to PI radians per second
        }

        // Gain function
        private fun gainFunc(v: Double, w: Double): Double {
            return 2 * zeta * sqrt((w * w) + ((b) * (v * v)))
        }
    }
}
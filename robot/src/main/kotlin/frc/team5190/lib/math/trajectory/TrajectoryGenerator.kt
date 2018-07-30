/*
 * FRC Team 5190
 * Green Hope Falcons
 */

/*
 * Some implementations and algorithms borrowed from:
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 */


package frc.team5190.lib.math.trajectory

import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Pose2dWithCurvature
import frc.team5190.lib.math.geometry.Rotation2d
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.timing.TimingConstraint
import frc.team5190.lib.math.trajectory.timing.TimingUtil
import frc.team5190.lib.math.trajectory.view.DistanceView

object TrajectoryGenerator {

    private const val kMaxDx = 2.0
    private const val kMaxDy = 0.25
    private const val kMaxDTheta = 0.1


    // Generate trajectory with custom start and end velocity.
    fun generateTrajectory(
            reversed: Boolean,
            waypoints: ArrayList<Pose2d>,
            constraints: ArrayList<TimingConstraint<Pose2dWithCurvature>>,
            startVel: Double,
            endVel: Double,
            maxVelocity: Double,
            maxAcceleration: Double
    ): Trajectory<TimedState<Pose2dWithCurvature>>? {

        // Make theta normal for trajectory generation if path is trajectoryReversed.
        if (reversed) {
            for (i in 0 until waypoints.size) {
                waypoints[i] = Pose2d(waypoints[i].translation, waypoints[i].rotation.rotateBy(Rotation2d.fromDegrees(180.0)))
            }
        }

        var trajectory = TrajectoryUtil.trajectoryFromSplineWaypoints(waypoints, kMaxDx, kMaxDy, kMaxDTheta)

        // After trajectory generation, flip theta back so it's relative to the field.
        if (reversed) {
            trajectory = TrajectoryUtil.transform(trajectory, Pose2d.fromRotation(Rotation2d(-1.0, 0.0, true)))
        }

        // Parameterize by time and return.
        return TimingUtil.timeParameterizeTrajectory(reversed, DistanceView(trajectory), kMaxDx, constraints,
                startVel, endVel, maxVelocity, maxAcceleration)
    }
}
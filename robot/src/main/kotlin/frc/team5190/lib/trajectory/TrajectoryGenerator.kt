/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.trajectory

import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Pose2dWithCurvature
import frc.team5190.lib.trajectory.timing.TimedState
import frc.team5190.lib.trajectory.timing.TimingConstraint
import frc.team5190.lib.trajectory.timing.TimingUtil

object TrajectoryGenerator {

    private const val MAX_DX = 2.0
    private const val MAX_DY = 0.25
    private const val MAX_DTHETA = 0.1


    fun generateTrajectory(
            reversed: Boolean,
            waypoints: MutableList<Pose2d>,
            constraints: List<TimingConstraint<Pose2dWithCurvature>>,
            maxVelocity: Double,
            maxAcceleration: Double
    ): Trajectory<TimedState<Pose2dWithCurvature>>? {

        return generateTrajectory(reversed, waypoints, constraints, 0.0, 0.0, maxVelocity, maxAcceleration)
    }

    private fun generateTrajectory(
            reversed: Boolean,
            waypoints: MutableList<Pose2d>,
            constraints: List<TimingConstraint<Pose2dWithCurvature>>,
            startVel: Double,
            endVel: Double,
            maxVelocity: Double,
            maxAcceleration: Double
    ): Trajectory<TimedState<Pose2dWithCurvature>>? {


        val trajectory = TrajectoryUtil.trajectoryFromSplineWaypoints(waypoints, MAX_DX, MAX_DY, MAX_DTHETA)
        val allConstraints = arrayListOf<TimingConstraint<Pose2dWithCurvature>>()

        allConstraints.addAll(constraints)

        return TimingUtil.timeParameterizeTrajectory(reversed, DistanceView(trajectory), MAX_DX, allConstraints,
                startVel, endVel, maxVelocity, maxAcceleration)
    }
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

@file:Suppress("unused")

package frc.team5190.robot.auto

import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Pose2dWithCurvature
import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.trajectory.Trajectory
import frc.team5190.lib.trajectory.timing.TimedState
import frc.team5190.lib.trajectory.timing.TimingConstraint
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

object Trajectories {

    // Generating reversed trajectories natively not supported at this time.

    // Constants
    private const val kSameSideAutoMaxVelocity = 10.0
    private const val kSameSideAutoMaxAcceleration = 6.0

    private const val kCrossAutoMaxVelocity = 8.0
    private const val kCrossAutoMaxAcceleration = 5.0

    private const val kSwitchAutoMaxVelocity = 6.0
    private const val kSwitchAutoMaxAcceleration = 3.0

    private const val kAfterFirstCubeMaxVelocity = 7.0
    private const val kAfterFirstCubeMaxAcceleration = 4.0


    // Waypoints
    private val sameSideAutoWaypointsFromLeft = mutableListOf<Pose2d>()
    private val crossAutoWaypointsFromLeft = mutableListOf(
            Pose2d(01.5, 23.5, Rotation2d()),
            Pose2d(10.0, 23.5, Rotation2d()),
            Pose2d(20.0, 16.5, Rotation2d.fromDegrees(-90.0)),
            Pose2d(20.0, 09.0, Rotation2d.fromDegrees(-90.0)),
            Pose2d(23.0, 07.0, Rotation2d.fromDegrees(010.0))
    )

    // Constraints
    private val sameSideAutoConstraints = mutableListOf<TimingConstraint<Pose2dWithCurvature>>()
    private val crossAutoConstraints = mutableListOf<TimingConstraint<Pose2dWithCurvature>>()
    private val switchAutoConstraints = mutableListOf<TimingConstraint<Pose2dWithCurvature>>()
    private val afterFirstCubeConstraints = mutableListOf<TimingConstraint<Pose2dWithCurvature>>()


    // Trajectories
    private val sameSideAutoTrajectory = async {
        return@async TrajectoryGenerator.generateTrajectory(
                false,
                sameSideAutoWaypointsFromLeft, sameSideAutoConstraints,
                kSameSideAutoMaxVelocity, kSameSideAutoMaxAcceleration
        )
    }

    private val crossAutoTrajectory = async {
        return@async TrajectoryGenerator.generateTrajectory(
                false,
                crossAutoWaypointsFromLeft, crossAutoConstraints,
                kCrossAutoMaxVelocity, kCrossAutoMaxAcceleration)
    }


    // Hash Map
    private val trajectories = hashMapOf(
            "Same Side Auto" to sameSideAutoTrajectory,
            "Cross Auto " to crossAutoTrajectory
    )

    init {
        launch { trajectories.values.awaitAll() }
    }

    operator fun get(identifier: String): Trajectory<TimedState<Pose2dWithCurvature>> = runBlocking {
        trajectories[identifier]?.await()!!
    }
}
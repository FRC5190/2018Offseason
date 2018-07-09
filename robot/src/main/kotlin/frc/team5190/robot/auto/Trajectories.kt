/*
 * FRC Team 5190
 * Green Hope Falcons
 */

@file:Suppress("unused")

package frc.team5190.robot.auto

import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Pose2dWithCurvature
import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.geometry.Translation2d
import frc.team5190.lib.trajectory.Trajectory
import frc.team5190.lib.trajectory.TrajectoryGenerator
import frc.team5190.lib.trajectory.timing.TimedState
import frc.team5190.lib.trajectory.timing.VelocityLimitRegionConstraint
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*

object Trajectories {

    // Constants in Feet Per Second
    private const val kMaxVelocity = 10.0
    private const val kMaxAcceleration = 5.0


    // Constraints
    private val kCrossAutoVelocityConstraint =
            Arrays.asList(VelocityLimitRegionConstraint<Pose2dWithCurvature>(
                    Translation2d(19.0, 6.5), Translation2d(23.0, 7.5), 3.0))



    // Field Relative Constants
    private val kStart = Pose2d(Translation2d(1.50, 23.5), Rotation2d())

    private val kNearScale = Pose2d(Translation2d(22.7, 20.0), Rotation2d.fromDegrees(-10.0))
    private val kFarScale  = Pose2d(Translation2d(22.7, 07.0), Rotation2d.fromDegrees(-10.0))

    private val kNearFenceCube1 = Pose2d()
    private val kNearFenceCube2 = Pose2d()
    private val kNearFenceCube3 = Pose2d()

    private val kFarFenceCube1 = Pose2d()
    private val kFarFenceCube2 = Pose2d()
    private val kFarFenceCube3 = Pose2d()

    private val kSwitchLeft  = Pose2d()
    private val kSwitchRight = Pose2d()


    // Waypoints
    private val kNearAutoWaypoints = mutableListOf(
            kStart, kStart.transformBy(Pose2d.fromTranslation(Translation2d(10.0, 0.0))), kNearScale
    )

    private val kCrossAutoWaypoints = mutableListOf(
            kStart,
            kStart.transformBy(Pose2d(Translation2d(13.0, +00.0), Rotation2d())),
            kStart.transformBy(Pose2d(Translation2d(18.3, -05.0), Rotation2d.fromDegrees(-90.0))),
            kStart.transformBy(Pose2d(Translation2d(18.3, -15.0), Rotation2d.fromDegrees(-90.0))),
            kFarScale
    )



    // Trajectories
    private val sameSideAutoTrajectory  = async {
        return@async TrajectoryGenerator.generateTrajectory(false, kCrossAutoWaypoints, listOf(), kMaxVelocity, kMaxAcceleration)
    }
    private val crossAutoTrajectory     = async {
        return@async TrajectoryGenerator.generateTrajectory(false, kNearAutoWaypoints, kCrossAutoVelocityConstraint, kMaxVelocity, kMaxAcceleration)
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
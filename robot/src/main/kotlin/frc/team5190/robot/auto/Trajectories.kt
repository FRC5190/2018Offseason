/*
 * FRC Team 5190
 * Green Hope Falcons
 */


package frc.team5190.robot.auto

import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Pose2dWithCurvature
import frc.team5190.lib.math.geometry.Rotation2d
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.lib.math.trajectory.Trajectory
import frc.team5190.lib.math.trajectory.TrajectoryGenerator
import frc.team5190.lib.math.trajectory.timing.CentripetalAccelerationConstraint
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.timing.TimingConstraint
import frc.team5190.robot.Constants.kCenterToFrontBumper
import frc.team5190.robot.Constants.kCenterToIntake
import frc.team5190.robot.Constants.kRobotCenterStartY
import frc.team5190.robot.Constants.kRobotSideStartY
import frc.team5190.robot.Constants.kRobotStartX
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.runBlocking

object Trajectories {

    // Constants in Feet Per Second
    private const val kMaxVelocity                = 8.0
    private const val kMaxAcceleration            = 4.0
    private const val kMaxCentripetalAcceleration = 4.0


    // Constraints
    private val kConstraints = mutableListOf(CentripetalAccelerationConstraint(kMaxCentripetalAcceleration))


    // Field Relative Constants
    internal val kSideStart               = Pose2d(Translation2d(kRobotStartX, kRobotSideStartY),   Rotation2d(-1.0, 0.0))
    internal val kCenterStart             = Pose2d(Translation2d(kRobotStartX, kRobotCenterStartY), Rotation2d())

    private val kNearScaleEmpty           = Pose2d(Translation2d(23.7, 20.2), Rotation2d.fromDegrees(170.0))
    internal val kNearScaleFull           = Pose2d(Translation2d(23.7, 20.2), Rotation2d.fromDegrees(165.0))

    private val kNearCube1                = Pose2d(Translation2d(16.5, 19.5), Rotation2d.fromDegrees(190.0))
    private val kNearCube2                = Pose2d(Translation2d(17.0, 17.0), Rotation2d.fromDegrees(245.0))
    private val kNearCube3                = Pose2d(Translation2d(16.8, 14.5), Rotation2d.fromDegrees(245.0))

    private val kNearCube1Adjusted        = kNearCube1.transformBy(kCenterToIntake)
    private val kNearCube2Adjusted        = kNearCube2.transformBy(kCenterToIntake)
    private val kNearCube3Adjusted        = kNearCube3.transformBy(kCenterToIntake)

    private val kSwitchLeft               = Pose2d(Translation2d(11.5, 18.8), Rotation2d())
    private val kSwitchRight              = Pose2d(Translation2d(11.5, 08.2), Rotation2d())

    internal val kSwitchLeftAdjusted      = kSwitchLeft .transformBy(kCenterToFrontBumper)
    private val kSwitchRightAdjusted      = kSwitchRight.transformBy(kCenterToFrontBumper)

    private val kFrontPyramidCube         = Pose2d(Translation2d(9.0, 13.5), Rotation2d())
    private val kFrontPyramidCubeAdjusted = kFrontPyramidCube.transformBy(kCenterToIntake)


    // Hash Map
    private val trajectories = hashMapOf<String, Deferred<Trajectory<TimedState<Pose2dWithCurvature>>>>()

    init {

        /* SOME TRAJECTORIES MAY NOT APPEAR HERE. THEY CAN BE OBTAINED BY MIRRORING
           TRAJECTORYUTIL.MIRRORTIMED(TRAJECTORY)
           ALL TRAJECTORIES UNLESS SPECIFIED GO TO LEFT SIDE OF GAME PIECE.
         */

        // Left Start to Near Scale
        mutableListOf(
                kSideStart,
                kSideStart.transformBy(Pose2d.fromTranslation(Translation2d(-10.0, 0.0))),
                kNearScaleEmpty
        ).also { generateTrajectory("Left Start to Near Scale", true, it) }


        // Left Start to Far Scale
        mutableListOf(
                kSideStart,
                kSideStart.transformBy(Pose2d(Translation2d(-13.0, 00.0), Rotation2d())),
                kSideStart.transformBy(Pose2d(Translation2d(-18.3, 05.0), Rotation2d.fromDegrees(-90.0))),
                kSideStart.transformBy(Pose2d(Translation2d(-18.3, 14.0), Rotation2d.fromDegrees(-90.0))),
                kNearScaleEmpty.mirror
        ).also { generateTrajectory("Left Start to Far Scale", true, it) }

        // Scale to Cube 1
        mutableListOf(
                kNearScaleEmpty,
                kNearCube1Adjusted
        ).also { generateTrajectory("Scale to Cube 1", false, it) }

        // Cube 1 to Scale
        mutableListOf(
                kNearCube1Adjusted,
                kNearScaleFull
        ).also { generateTrajectory("Cube 1 to Scale", true, it) }

        // Scale to Cube 2
        mutableListOf(
                kNearScaleFull,
                kNearCube2Adjusted
        ).also { generateTrajectory("Scale to Cube 2", false, it) }

        // Cube 2 to Scale
        mutableListOf(
                kNearCube2Adjusted,
                kNearScaleFull
        ).also { generateTrajectory("Cube 2 to Scale", true, it) }

        // Scale to Cube 3
        mutableListOf(
                kNearScaleFull,
                kNearCube3Adjusted
        ).also { generateTrajectory("Scale to Cube 3", false, it) }

        // Cube 3 to Scale
        mutableListOf(
                kNearCube3Adjusted,
                kNearScaleFull
        ).also { generateTrajectory("Cube 3 to Scale", true, it) }

        // Center Start to Left Switch
        mutableListOf(
                kCenterStart,
                kSwitchLeftAdjusted
        ).also { generateTrajectory("Center Start to Left Switch", false, it) }

        // Center Start to Right Switch
        mutableListOf(
                kCenterStart,
                kSwitchRightAdjusted
        ).also { generateTrajectory("Center Start to Right Switch", false, it) }

        // Switch to Center
        mutableListOf(
                kSwitchLeftAdjusted,
                kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
        ).also { generateTrajectory("Switch to Center", true, it) }

        // Center to Pyramid
        mutableListOf(
                kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0))),
                kFrontPyramidCubeAdjusted
        ).also { generateTrajectory("Center to Pyramid", false, it) }

        // Pyramid to Center
        mutableListOf(
                kFrontPyramidCubeAdjusted,
                kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
        ).also { generateTrajectory("Pyramid to Center", true, it) }

        // Center to Switch
        mutableListOf(
                kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0))),
                kSwitchLeftAdjusted
        ).also { generateTrajectory("Center to Switch", false, it) }

        // Pyramid to Scale
        mutableListOf(
                kFrontPyramidCubeAdjusted,
                kFrontPyramidCubeAdjusted.transformBy(Pose2d(Translation2d(0.0, 9.0), Rotation2d.fromDegrees(180.0))),
                kFrontPyramidCubeAdjusted.transformBy(Pose2d(Translation2d(7.0, 9.0), Rotation2d.fromDegrees(180.0))),
                kNearScaleEmpty
        ).also { generateTrajectory("Pyramid to Scale", true, it) }

        // Baseline
        mutableListOf(
                kSideStart,
                kSideStart.transformBy(Pose2d(Translation2d(-10.0, 0.0), Rotation2d()))
        ).also { generateTrajectory("Baseline", true, it) }


        runBlocking {
            trajectories.values.awaitAll()
        }
    }

    operator fun get(identifier: String): Trajectory<TimedState<Pose2dWithCurvature>> = runBlocking {
        trajectories[identifier]?.await()!!
    }

    private fun generateTrajectory(name: String,
                                   reversed: Boolean,
                                   waypoints: MutableList<Pose2d>,
                                   maxVelocity: Double = kMaxVelocity,
                                   maxAcceleration: Double = kMaxAcceleration,
                                   constraints: List<TimingConstraint<Pose2dWithCurvature>> = kConstraints
    ) {
        trajectories[name] = async {
            val start = System.nanoTime()
            TrajectoryGenerator.generateTrajectory(reversed, waypoints, constraints, 0.0, 0.0, maxVelocity, maxAcceleration)!!.also {
                System.out.printf("[Trajectory Generator] Generation of %-35s took %3.3f milliseconds.%n", "\"$name\"", (System.nanoTime() - start) / 1000000.0)
            }
        }
    }
}
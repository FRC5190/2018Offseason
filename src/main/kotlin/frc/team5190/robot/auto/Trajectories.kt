/*
 * FRC Team 5190
 * Green Hope Falcons
 */


package frc.team5190.robot.auto

import frc.team5190.lib.math.geometry.*
import frc.team5190.lib.math.trajectory.AStarOptimizer
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

object Trajectories {

    // Constants in Feet Per Second
    private const val kMaxVelocity = 10.0
    private const val kMaxAcceleration = 4.0
    private const val kMaxCentripetalAcceleration = 4.0

    // Constraints
    private val kConstraints = arrayListOf<TimingConstraint<Pose2dWithCurvature>>(
            CentripetalAccelerationConstraint(kMaxCentripetalAcceleration))

    // Field Relative Constants
    internal val kSideStart = Pose2d(Translation2d(kRobotStartX, kRobotSideStartY), Rotation2d(-1.0, 0.0))
    internal val kCenterStart = Pose2d(Translation2d(kRobotStartX, kRobotCenterStartY), Rotation2d())

    private val kNearScaleEmpty = Pose2d(Translation2d(23.7, 20.2), Rotation2d.fromDegrees(170.0))
    internal val kNearScaleFull = Pose2d(Translation2d(23.7, 20.2), Rotation2d.fromDegrees(165.0))

    private val kNearCube1 = Pose2d(Translation2d(16.5, 19.5), Rotation2d.fromDegrees(190.0))
    private val kNearCube2 = Pose2d(Translation2d(17.0, 17.0), Rotation2d.fromDegrees(220.0))
    private val kNearCube3 = Pose2d(Translation2d(16.8, 14.5), Rotation2d.fromDegrees(245.0))

    private val kNearCube1Adjusted = kNearCube1.transformBy(kCenterToIntake)
    private val kNearCube2Adjusted = kNearCube2.transformBy(kCenterToIntake)
    private val kNearCube3Adjusted = kNearCube3.transformBy(kCenterToIntake)

    private val kSwitchLeft = Pose2d(Translation2d(11.5, 18.2), Rotation2d())
    private val kSwitchRight = Pose2d(Translation2d(11.5, 08.8), Rotation2d())

    internal val kSwitchLeftAdjusted = kSwitchLeft.transformBy(kCenterToFrontBumper)
    private val kSwitchRightAdjusted = kSwitchRight.transformBy(kCenterToFrontBumper)

    private val kFrontPyramidCube = Pose2d(Translation2d(10.25, 13.5), Rotation2d())
    private val kFrontPyramidCubeAdjusted = kFrontPyramidCube.transformBy(kCenterToIntake)


    // Trajectories
    val leftStartToNearScale = waypoints {
        +kSideStart
        +kSideStart.transformBy(Pose2d.fromTranslation(Translation2d(-10.0, 0.0)))
        +kNearScaleEmpty
    }.generateTrajectory(reversed = true)

    val leftStartToFarScale = waypoints {
        +kSideStart
        +kSideStart.transformBy(Pose2d(Translation2d(-13.0, 00.0), Rotation2d()))
        +kSideStart.transformBy(Pose2d(Translation2d(-18.3, 05.0), Rotation2d.fromDegrees(-90.0)))
        +kSideStart.transformBy(Pose2d(Translation2d(-18.3, 14.0), Rotation2d.fromDegrees(-90.0)))
        +kNearScaleEmpty.mirror
    }.generateTrajectory(reversed = true)

    val scaleToCube1 = waypoints {
        +kNearScaleEmpty
        +kNearCube1Adjusted
    }.generateTrajectory(reversed = false)

    val cube1ToScale = waypoints {
        +kNearCube1Adjusted
        +kNearScaleFull
    }.generateTrajectory(reversed = true)

    val scaleToCube2 = waypoints {
        +kNearScaleFull
        +kNearCube2Adjusted
    }.generateTrajectory(reversed = false)

    val cube2ToScale = waypoints {
        +kNearCube2Adjusted
        +kNearScaleFull
    }.generateTrajectory(reversed = true)

    val scaleToCube3 = waypoints {
        +kNearScaleFull
        +kNearCube3Adjusted
    }.generateTrajectory(reversed = false)


    val cube3ToScale = waypoints {
        +kNearCube3Adjusted
        +kNearScaleFull
    }.generateTrajectory(reversed = true)

    val centerStartToLeftSwitch = waypoints {
        +kCenterStart
        +kSwitchLeftAdjusted
    }.generateAStar(reversed = false)

    val centerStartToRightSwitch = waypoints {
        +kCenterStart
        +kSwitchRightAdjusted
    }.generateTrajectory(reversed = false)

    val switchToCenter = waypoints {
        +kSwitchLeftAdjusted
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
    }.generateTrajectory(reversed = true)

    val centerToPyramid = waypoints {
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
        +kFrontPyramidCubeAdjusted
    }.generateTrajectory(reversed = false)

    val pyramidToCenter = waypoints {
        +kFrontPyramidCubeAdjusted
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
    }.generateTrajectory(reversed = true)

    val centerToSwitch = waypoints {
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
        +kSwitchLeftAdjusted
    }.generateTrajectory(reversed = false)

    val pyramidToScale = waypoints {
        +kFrontPyramidCubeAdjusted
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d(Translation2d(0.0, 9.0), Rotation2d.fromDegrees(180.0)))
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d(Translation2d(7.0, 9.0), Rotation2d.fromDegrees(180.0)))
        +kNearScaleEmpty
    }.generateTrajectory(reversed = true)

    val baseline = waypoints {
        +kSideStart
        +kSideStart.transformBy(Pose2d(Translation2d(-8.0, 0.0), Rotation2d()))
    }.generateTrajectory(reversed = true)


    private class Waypoints {
        val points = ArrayList<Pose2d>()

        fun generateTrajectory(reversed: Boolean,
                               maxVelocity: Double = kMaxVelocity,
                               maxAcceleration: Double = kMaxAcceleration,
                               constraints: ArrayList<TimingConstraint<Pose2dWithCurvature>> = kConstraints): Trajectory<TimedState<Pose2dWithCurvature>> {

            return TrajectoryGenerator.generateTrajectory(
                    reversed = reversed,
                    waypoints = points,
                    constraints = constraints,
                    startVel = 0.0,
                    endVel = 0.0,
                    maxVelocity = maxVelocity,
                    maxAcceleration = maxAcceleration)!!
        }

        fun generateAStar(reversed: Boolean,
                          maxVelocity: Double = kMaxVelocity,
                          maxAcceleration: Double = kMaxAcceleration,
                          constraints: ArrayList<TimingConstraint<Pose2dWithCurvature>> = kConstraints): Trajectory<TimedState<Pose2dWithCurvature>> {

            if (points.size != 2) generateTrajectory(reversed, maxVelocity, maxAcceleration, constraints)

            val kRobotSize = 3.0 // 2.75

            val kLeftSwitch = Rectangle2d(140.0 / 12.0, 85.25 / 12.0, 56.0 / 12.0, 153.5 / 12.0)
            val kPlatform = Rectangle2d(261.47 / 12.0, 95.25 / 12.0, 125.06 / 12.0, 133.5 / 12.0)
            val kRightSwitch = Rectangle2d(54 - (kLeftSwitch.x + kLeftSwitch.w), kLeftSwitch.y, kLeftSwitch.w, kLeftSwitch.h)

            val optimizedPoints = AStarOptimizer(kRobotSize, kLeftSwitch, kPlatform, kRightSwitch).optimize(points[0], points[1])!!.path as ArrayList<Pose2d>

            optimizedPoints.forEach {println(it)}

            return TrajectoryGenerator.generateTrajectory(
                    reversed = reversed,
                    waypoints = optimizedPoints,
                    constraints = constraints,
                    startVel = 0.0,
                    endVel = 0.0,
                    maxVelocity = maxVelocity,
                    maxAcceleration = maxAcceleration)!!

        }

        operator fun Pose2d.unaryPlus() {
            points.add(this)
        }
    }

    private fun waypoints(block: Waypoints.() -> Unit): Waypoints {
        val waypoints = Waypoints(); block(waypoints); return waypoints
    }
}
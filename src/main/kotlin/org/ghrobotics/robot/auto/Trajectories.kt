/*
 * FRC Team 5190
 * Green Hope Falcons
 */


package org.ghrobotics.robot.auto

import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Rotation2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.TimedState
import org.ghrobotics.lib.mathematics.twodim.trajectory.Trajectory
import org.ghrobotics.lib.mathematics.twodim.trajectory.TrajectoryGenerator
import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.CentripetalAccelerationConstraint
import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.TimingConstraint
import org.ghrobotics.robot.Constants.kCenterToFrontBumper
import org.ghrobotics.robot.Constants.kCenterToIntake
import org.ghrobotics.robot.Constants.kRobotCenterStartY
import org.ghrobotics.robot.Constants.kRobotSideStartY
import org.ghrobotics.robot.Constants.kRobotStartX

object Trajectories {

    // Constants in Feet Per Second
    private const val kMaxVelocity = 8.0
    private const val kMaxAcceleration = 4.0
    private const val kMaxCentripetalAcceleration = 4.5

    // Constraints
    private val kConstraints = arrayListOf<TimingConstraint<Pose2dWithCurvature>>(
            CentripetalAccelerationConstraint(kMaxCentripetalAcceleration))

    // Field Relative Constants
    internal val kSideStart = Pose2d(Translation2d(kRobotStartX, kRobotSideStartY), Rotation2d(-1.0, 0.0))
    internal val kCenterStart = Pose2d(Translation2d(kRobotStartX, kRobotCenterStartY), Rotation2d())

    private val kNearScaleEmpty = Pose2d(Translation2d(23.95, 20.2), Rotation2d.fromDegrees(160.0))
    private val kNearScaleFull = Pose2d(Translation2d(23.95, 20.0), Rotation2d.fromDegrees(170.0))
    private val kNearScaleFullInner = Pose2d(Translation2d(24.3, 20.0), Rotation2d.fromDegrees(170.0))

    private val kNearCube1 = Pose2d(Translation2d(16.5, 19.2), Rotation2d.fromDegrees(190.0))
    private val kNearCube2 = Pose2d(Translation2d(17.4, 15.0), Rotation2d.fromDegrees(245.0))
    private val kNearCube3 = Pose2d(Translation2d(17.6, 14.5), Rotation2d.fromDegrees(245.0))

    private val kNearCube1Adjusted = kNearCube1.transformBy(kCenterToIntake)
    private val kNearCube2Adjusted = kNearCube2.transformBy(kCenterToIntake)
    private val kNearCube3Adjusted = kNearCube3.transformBy(kCenterToIntake)

    private val kFarCube1 = Pose2d(Translation2d(16.5, 20.0), Rotation2d.fromDegrees(195.0))
    private val kFarCube1Adjusted = kFarCube1.transformBy(kCenterToIntake)

    private val kSwitchLeft = Pose2d(Translation2d(11.9, 18.5), Rotation2d())
    private val kSwitchRight = Pose2d(Translation2d(11.9, 08.5), Rotation2d())

    private val kSwitchLeftAdjusted = kSwitchLeft.transformBy(kCenterToFrontBumper)
    private val kSwitchRightAdjusted = kSwitchRight.transformBy(kCenterToFrontBumper)

    private val kFrontPyramidCube = Pose2d(Translation2d(10.25, 13.5), Rotation2d())
    private val kFrontPyramidCubeAdjusted = kFrontPyramidCube.transformBy(kCenterToIntake)

    internal val trajectories = arrayListOf<Container>()

    // FastTrajectories
    val leftStartToNearScale = waypoints {
        +kSideStart
        +kSideStart.transformBy(Pose2d.fromTranslation(Translation2d(-10.0, 0.0)))
        +kNearScaleEmpty
    }.generateTrajectory("Left Start to Near Scale", reversed = true)

    val leftStartToFarScale = waypoints {
        +kSideStart
        +kSideStart.transformBy(Pose2d(Translation2d(-13.0, 00.0), Rotation2d()))
        +kSideStart.transformBy(Pose2d(Translation2d(-18.9, 05.0), Rotation2d.fromDegrees(-90.0)))
        +kSideStart.transformBy(Pose2d(Translation2d(-18.9, 14.0), Rotation2d.fromDegrees(-90.0)))
        +kNearScaleEmpty.mirror
    }.generateTrajectory("Left Start to Far Scale", reversed = true)

    val scaleToCube1 = waypoints {
        +kNearScaleEmpty
        +kNearCube1Adjusted
    }.generateTrajectory("Scale to Cube 1", reversed = false)

    val cube1ToScale = waypoints {
        +kNearCube1Adjusted
        +kNearScaleFull
    }.generateTrajectory("Cube 1 to Scale", reversed = true)

    val scaleToCube2 = waypoints {
        +kNearScaleFull
        +kNearCube2Adjusted
    }.generateTrajectory("Scale to Cube 2", reversed = false)

    val cube2ToScale = waypoints {
        +kNearCube2Adjusted
        +kNearScaleFullInner
    }.generateTrajectory("Cube 2 to Scale", reversed = true)

    val scaleToCube3 = waypoints {
        +kNearScaleFull
        +kNearCube3Adjusted
    }.generateTrajectory("Scale to Cube 3", reversed = false)

    val scaleToFar1 = waypoints {
        +kNearScaleEmpty
        +kFarCube1Adjusted
    }.generateTrajectory("Scale to Far 1", reversed = false)

    val far1ToScale = waypoints {
        +kFarCube1Adjusted
        +kNearScaleFullInner
    }.generateTrajectory("Far 1 to Scale", reversed = true)

    val cube3ToScale = waypoints {
        +kNearCube3Adjusted
        +kNearScaleFullInner
    }.generateTrajectory("Cube 3 to Scale", reversed = true)


    val centerStartToLeftSwitch = waypoints {
        +kCenterStart
        +kSwitchLeftAdjusted
    }.generateTrajectory("Center Start to Left Switch", reversed = false)

    val centerStartToRightSwitch = waypoints {
        +kCenterStart
        +kSwitchRightAdjusted
    }.generateTrajectory("Center Start to Right Switch", reversed = false)

    val switchToCenter = waypoints {
        +kSwitchLeftAdjusted
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
    }.generateTrajectory("Switch to Center", reversed = true)

    val centerToPyramid = waypoints {
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
        +kFrontPyramidCubeAdjusted
    }.generateTrajectory("Center to Pyramid", reversed = false)

    val pyramidToCenter = waypoints {
        +kFrontPyramidCubeAdjusted
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
    }.generateTrajectory("Pyramid to Center", reversed = true)

    val centerToSwitch = waypoints {
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d.fromTranslation(Translation2d(-4.0, 0.0)))
        +kSwitchLeftAdjusted
    }.generateTrajectory("Center to Switch", reversed = false)

    val pyramidToScale = waypoints {
        +kFrontPyramidCubeAdjusted
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d(Translation2d(2.0, 9.0), Rotation2d.fromDegrees(180.0)))
        +kFrontPyramidCubeAdjusted.transformBy(Pose2d(Translation2d(7.0, 9.0), Rotation2d.fromDegrees(180.0)))
        +kNearScaleEmpty
    }.generateTrajectory("Pyramid to Scale", reversed = true, maxVelocity = 4.0, maxAcceleration = 3.0,
            constraints = arrayListOf(CentripetalAccelerationConstraint(3.0)))

    val baseline = waypoints {
        +kSideStart
        +kSideStart.transformBy(Pose2d(Translation2d(-8.0, 0.0), Rotation2d()))
    }.generateTrajectory("Baseline", reversed = true)

    private class Waypoints {
        val points = ArrayList<Pose2d>()

        fun generateTrajectory(name: String,
                               reversed: Boolean,
                               maxVelocity: Double = kMaxVelocity,
                               maxAcceleration: Double = kMaxAcceleration,
                               constraints: ArrayList<TimingConstraint<Pose2dWithCurvature>> = kConstraints): Trajectory<TimedState<Pose2dWithCurvature>> {

            return TrajectoryGenerator.generateTrajectory(
                    reversed = reversed,
                    wayPoints = points,
                    constraints = constraints,
                    startVel = 0.0,
                    endVel = 0.0,
                    maxVelocity = maxVelocity,
                    maxAcceleration = maxAcceleration)!!.also { trajectories.add(Container(name, it)) }
        }

        operator fun Pose2d.unaryPlus() {
            points.add(this)
        }
    }

    private fun waypoints(block: Waypoints.() -> Unit): Waypoints {
        val waypoints = Waypoints(); block(waypoints); return waypoints
    }

    data class Container(private val name: String, val trajectory: Trajectory<TimedState<Pose2dWithCurvature>>) {
        override fun toString() = name
    }
}

package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.control.VelocityPIDFController
import frc.team5190.lib.control.TrajectoryFollower
import frc.team5190.lib.kinematics.Translation2d
import frc.team5190.robot.Kinematics
import frc.team5190.robot.Localization
import frc.team5190.robot.PathGenerator
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

class FollowTrajectoryCommand(file: String,
                              private val robotReversed: Boolean = false,
                              private val pathMirrored: Boolean = false,
                              private val pathReversed: Boolean = false,
                              private val resetRobotPosition: Boolean = false) : Command() {

    // Notifier objects
    private val pf = Object()
    private val notifier: Notifier
    private var stopNotifier = false

    // Trajectory
    private val trajectory = PathGenerator[file]!!

    // Path follower
    private val trajectoryFollower: TrajectoryFollower

    // PIDF controllers
    private val lController = VelocityPIDFController()
    private val rController = VelocityPIDFController()

    init {
        requires(DriveSubsystem)

        // Modify trajectory if reversed or mirrored
        modifyTrajectory()

        // Initialize path follower
        trajectoryFollower = TrajectoryFollower(trajectory = trajectory)

        // Set PIDF Values
        lController.apply {
            p = 0.08
            i = 0.01
            v = 0.05
            vIntercept = 0.1
        }
        rController.apply {
            p = 0.08
            i = 0.01
            v = 0.05
            vIntercept = 0.1
        }

        // Initialize notifier
        notifier = Notifier {
            synchronized(pf) {
                if (stopNotifier) {
                    return@Notifier
                }

                // Get left and right wheel outputs
                val output = Kinematics.inverseKinematics(
                        trajectoryFollower.getRobotVelocity(Localization.robotPosition))

                // Update PIDF controller setpoints
                val l = lController.getPIDFOutput(target = output.left, actual = DriveSubsystem.leftVelocity.FPS.value)
                val r = rController.getPIDFOutput(target = output.right, actual = DriveSubsystem.rightVelocity.FPS.value)

                // Set drive motors and update companion values
                DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = l, rightOutput = r)
                updateDashboard()
            }
        }
    }

    // Modifies trajectories based on reversed / mirrored states.
    private fun modifyTrajectory() {
        // Reverse the order of segments when the path is reversed
        if (pathReversed) {
            val reversedTrajectory = trajectory.copy()
            val distance = reversedTrajectory.segments.last().position

            reversedTrajectory.segments.reverse()
            reversedTrajectory.segments.forEach { it.position = distance - it.position }

            trajectory.segments = reversedTrajectory.segments
        }

        trajectory.segments.forEach { segment ->
            fun addPiToHeadings() {
                var newHeading = segment.heading + Math.PI
                if (newHeading > 2 * Math.PI) newHeading -= 2 * Math.PI

                segment.heading = newHeading
            }

            // Mirror headings if path is mirrored
            if (pathMirrored) {
                segment.heading = -segment.heading + (2 * Math.PI)
                segment.y = 27 - segment.y
            }
            // Add PI to the headings if path is reversed
            if (pathReversed) {
                addPiToHeadings()
            }
            // Negate derivatives if robot is reversed and add PI to headings if robot is reversed
            if (robotReversed) {
                addPiToHeadings()
                segment.position = -segment.position
                segment.velocity = -segment.velocity
                segment.acceleration = -segment.acceleration
                segment.jerk = -segment.jerk
            }
        }
    }

    fun addMarkerAt(pos: Vector2D): Marker {
        val waypoint = if (pathMirrored) Vector2D(pos.x, 27 - pos.y) else pos
        return Marker(this, trajectory.segments.minBy { waypoint.distance(Vector2D(it.x, it.y)) }!!.position)
    }

    fun hasPassedMarker(marker: Marker): Boolean {
        return marker.commandInstance == this && trajectoryFollower.currentSegment.position >= marker.position
    }

    private fun updateDashboard() {
        pathX = trajectoryFollower.currentSegment.x
        pathY = trajectoryFollower.currentSegment.y
        pathHdg = trajectoryFollower.currentSegment.heading

        lookaheadX = trajectoryFollower.currentSegment.x
        lookaheadY = trajectoryFollower.currentSegment.y
    }

    override fun initialize() {
        if (resetRobotPosition) {
            DriveSubsystem.resetEncoders()
            Localization.reset(translation2d = Translation2d(trajectory.segments[0].x, trajectory.segments[0].y))
        }
        notifier.startPeriodic(trajectory.segments[0].dt)
    }

    override fun end() {
        synchronized(pf) {
            stopNotifier = true
            notifier.stop()
            DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = 0.0, rightOutput = 0.0)
        }
    }

    override fun isFinished() = trajectoryFollower.isFinished

    companion object {
        var pathX = 0.0
            private set
        var pathY = 0.0
            private set
        var pathHdg = 0.0
            private set

        var lookaheadX = 0.0
            private set
        var lookaheadY = 0.0
            private set
    }
}

class Marker(val commandInstance: FollowTrajectoryCommand, val position: Double)
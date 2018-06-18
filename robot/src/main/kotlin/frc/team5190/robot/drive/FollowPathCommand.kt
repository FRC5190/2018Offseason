package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.control.PIDFController
import frc.team5190.lib.control.PathFollower
import frc.team5190.lib.util.Pathreader
import frc.team5190.robot.Kinematics
import frc.team5190.robot.Localization
import frc.team5190.robot.sensors.NavX
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

class FollowPathCommand(folder: String, file: String,
                        private val robotReversed: Boolean = false,
                        private val pathMirrored: Boolean = false,
                        private val pathReversed: Boolean = false,
                        private val resetRobotPosition: Boolean = false) : Command() {

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

    // Notifier objects
    private val pf = Object()
    private val notifier: Notifier
    private var stopNotifier = false

    // Trajectory
    private val trajectory = Pathreader.getPath(folder, file)

    // Path follower
    private val pathFollower: PathFollower

    // PIDF controllers
    private val lController = PIDFController()
    private val rController = PIDFController()

    init {
        requires(DriveSubsystem)

        // Modify trajectory if reversed or mirrored
        modifyTrajectory()

        // Initialize path follower
        pathFollower = PathFollower(trajectory = trajectory)

        // Set PIDF Values
        lController.apply {
            p = 0.15
            v = 0.045
            vIntercept = 0.1
        }
        rController.apply {
            p = 0.15
            v = 0.045
            vIntercept = 0.1
        }

        // Initialize notifier
        notifier = Notifier {
            synchronized(pf) {
                if (stopNotifier) {
                    return@Notifier
                }

                val output = Kinematics.inverseKinematics(pathFollower.getLinAndAngVelocities(
                        pose = Localization.robotPosition,
                        theta = Math.toRadians(NavX.correctedAngle)))

                val l = lController.getPIDFOutput(target = output.left, actual = DriveSubsystem.leftVelocity.FPS.value)
                val r = rController.getPIDFOutput(target = output.right, actual = DriveSubsystem.rightVelocity.FPS.value)

                DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = l, rightOutput = r)
            }
        }
    }

    private fun modifyTrajectory() {
        if (pathReversed) {
            val reversedTrajectory = trajectory.copy()
            val distance = reversedTrajectory.segments.last().position

            reversedTrajectory.segments.reverse()
            reversedTrajectory.segments.forEach { it.position = distance - it.position }

            trajectory.segments = reversedTrajectory.segments
        }

        trajectory.segments.forEach { segment ->
            if (pathMirrored) {
                segment.heading = -segment.heading + (2 * Math.PI)
                segment.y = 27 - segment.y
            }
            if (robotReversed xor pathReversed) {
                var newHeading = segment.heading + Math.PI
                if (newHeading > 2 * Math.PI) newHeading -= 2 * Math.PI

                segment.heading = newHeading
            }
        }
    }

    private fun updateDashboard() {
        pathX = pathFollower.currentSegment.x
        pathY = pathFollower.currentSegment.y
        pathHdg = pathFollower.currentSegment.heading

        lookaheadX = pathFollower.currentSegment.x
        lookaheadY = pathFollower.currentSegment.y
    }

    override fun initialize() {
        DriveSubsystem.resetEncoders()
        if (resetRobotPosition) {
            Localization.reset(position = Vector2D(trajectory.segments[0].x, trajectory.segments[0].y))
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

    override fun isFinished() = pathFollower.isFinished
}

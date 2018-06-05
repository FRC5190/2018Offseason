package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.control.PathFollower
import frc.team5190.lib.util.Pathreader
import frc.team5190.robot.Localization
import frc.team5190.robot.sensors.Pigeon
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

class FollowPathCommand(folder: String, file: String,
                        robotReversed: Boolean = false,
                        private val pathMirrored: Boolean = false,
                        pathReversed: Boolean = false,
                        private val resetRobotPosition: Boolean = false) : Command() {

    companion object {
        const val DT = 0.02

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
    private val synchronousNotifier = Object()
    private val notifier: Notifier
    private var stopNotifier = false

    // Left, right, and source trajectories
    private val trajectories = Pathreader.getPaths(folder, file)

    // Path follower
    private val pathFollower: PathFollower

    init {
        requires(DriveSubsystem)

        // Modify trajectories if reversed or mirrored
        trajectories.forEach { trajectory ->
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

        if (pathMirrored xor (pathReversed xor robotReversed)) {
            val leftTrajectory = trajectories[0]
            trajectories[0] = trajectories[1]
            trajectories[1] = leftTrajectory
        }

        // Initialize path follower
        pathFollower = PathFollower(
                leftTrajectory = trajectories[0],
                rightTrajectory = trajectories[1],
                sourceTrajectory = trajectories[2],
                reversed = robotReversed).apply {

            p = 0.5
            v = 0.059
            vIntercept = 0.10
            pTurn = 0.0847
        }

        // Initialize notifier
        notifier = Notifier {
            synchronized(synchronousNotifier) {
                if (stopNotifier) {
                    return@Notifier
                }

                pathX = pathFollower.currentSegment.x
                pathY = pathFollower.currentSegment.y
                pathHdg = pathFollower.currentSegment.heading

                lookaheadX = pathFollower.lookaheadSegment.x
                lookaheadY = pathFollower.lookaheadSegment.y

                val output = pathFollower.getMotorOutput(
                        robotPosition = Localization.robotPosition,
                        robotAngle = Pigeon.correctedAngle,
                        rawEncoderVelocities = DriveSubsystem.leftVelocity to DriveSubsystem.rightVelocity)

                DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = output.first, rightOutput = output.second)
            }
        }
    }

    // Adds a marker at the point along the path where @pos is closest to that point
    fun addMarkerAt(pos: Vector2D): Marker {
        val waypoint = if (pathMirrored) Vector2D(pos.x, 27 - pos.y) else pos
        return Marker(trajectories[2].segments.minBy { waypoint.distance(Vector2D(it.x, it.y)) }!!.position)
    }

    fun hasPassedMarker(marker: Marker): Boolean {
        return pathFollower.currentSegment.position >= marker.position
    }

    override fun initialize() {
        DriveSubsystem.resetEncoders()
        if (resetRobotPosition) {
            Localization.reset(startingPosition = Vector2D(trajectories[2].segments[0].x, trajectories[2].segments[0].y))
        }
        notifier.startPeriodic(DT)
    }

    override fun end() {
        synchronized(synchronousNotifier) {
            stopNotifier = true
            notifier.stop()
            DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = 0.0, rightOutput = 0.0)

            pathX = 0.0
            pathY = 0.0
            pathHdg = 0.0

            lookaheadX = 0.0
            lookaheadY = 0.0
        }
    }
    override fun isFinished() = pathFollower.isFinished
}

class Marker(val position: Double)
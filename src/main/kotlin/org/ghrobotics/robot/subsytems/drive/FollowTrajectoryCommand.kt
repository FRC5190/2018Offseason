/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import kotlinx.coroutines.experimental.GlobalScope
import org.ghrobotics.lib.commands.Command
import org.ghrobotics.lib.mathematics.twodim.control.RamseteController
import org.ghrobotics.lib.mathematics.twodim.control.TrajectoryFollower
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedEntry
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedTrajectory
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TrajectorySamplePoint
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.mirror
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.mathematics.units.meter
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.utils.BooleanSource
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.observabletype.ObservableValue
import org.ghrobotics.lib.utils.observabletype.ObservableVariable
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.Localization

class FollowTrajectoryCommand(
    val trajectory: Source<TimedTrajectory<Pose2dWithCurvature>>,
    pathMirrored: BooleanSource = Source(false)
) : Command(DriveSubsystem) {

    constructor(
        trajectory: TimedTrajectory<Pose2dWithCurvature>,
        pathMirrored: BooleanSource = Source(false)
    ) : this(Source(trajectory), pathMirrored)

    private val trajectoryFollower: TrajectoryFollower

    private val markerLocations = mutableListOf<Marker>()
    private val markers = mutableListOf<MarkerInternal>()

    private val trajectoryUsed: TimedTrajectory<Pose2dWithCurvature>

    init {
        // Update the frequency of the command to the follower
        executeFrequency = (1.second / deltaTime).toInt() // Hz

        // Add markers
        markers.clear()

        trajectoryUsed = if (pathMirrored.value) trajectory.value.mirror() else trajectory.value

        // Iterate through the trajectory and add a data point every 50 ms.
        val iterator = trajectoryUsed.iterator()
        val dataArray = arrayListOf<TrajectorySamplePoint<TimedEntry<Pose2dWithCurvature>>>()

        while (!iterator.isDone) {
            dataArray.add(iterator.advance(deltaTime))
        }
        markerLocations.forEach { marker ->
            val condition = marker.condition as ObservableVariable<Boolean>
            condition.value = false // make sure its false

            val usedLocation = marker.location.value
            markers.add(
                MarkerInternal(
                    dataArray.minBy { usedLocation.distance(it.state.state.pose.translation) }!!.state.t,
                    condition
                )
            )
        }

        // Initialize path follower
        trajectoryFollower = RamseteController(
            trajectoryUsed,
            Constants.kDifferentialDrive,
            Constants.kDriveBeta,
            Constants.kDriveZeta
        )

        _finishCondition += GlobalScope.updatableValue { trajectoryFollower.isFinished }
    }

    fun addMarkerAt(location: Translation2d) = addMarkerAt(Source(location))
    fun addMarkerAt(location: Source<Translation2d>) =
        Marker(location, ObservableVariable(false)).also { markerLocations.add(it) }

    private fun updateDashboard() {
        pathX = trajectoryFollower.referencePose.translation.x
        pathY = trajectoryFollower.referencePose.translation.y
        pathHdg = trajectoryFollower.referencePose.rotation

        lookaheadX = pathX
        lookaheadY = pathY
    }

    override suspend fun execute() {
        val position = Localization.robotPosition

        val output = trajectoryFollower.getOutputFromDynamics(position)

        DriveSubsystem.setTrajectoryVelocity(output)

        updateDashboard()

        // Update marker states
        val followerStateTime = trajectoryFollower.referencePoint.state.t
        markers.forEach { it.condition.value = followerStateTime > it.t }
    }

    override suspend fun dispose() {
        DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = 0.0, rightOutput = 0.0)
        println(
            "[Trajectory Follower] " +
                    "Norm of Translational Error: " +
                    "${(Localization.robotPosition.translation - trajectoryUsed.lastState.state.pose.translation).norm}"
        )
        println(
            "[Trajectory Follower]" +
                    "Rotation Error: " +
                    "${(Localization.robotPosition.rotation - trajectoryUsed.lastState.state.pose.rotation).degree.asDouble} degrees."
        )

    }

    companion object {
        val deltaTime = 50.millisecond

        var pathX = 0.meter
            private set
        var pathY = 0.meter
            private set
        var pathHdg = 0.degree
            private set

        var lookaheadX = 0.meter
            private set
        var lookaheadY = 0.meter
            private set
    }

    class Marker(val location: Source<Translation2d>, val condition: ObservableValue<Boolean>)
    private class MarkerInternal(val t: Double, val condition: ObservableVariable<Boolean>)
}
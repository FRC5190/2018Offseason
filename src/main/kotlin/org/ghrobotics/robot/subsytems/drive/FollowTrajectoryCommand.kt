/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.mathematics.twodim.control.NonLinearController
import org.ghrobotics.lib.mathematics.twodim.control.TrajectoryFollower
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.*
import org.ghrobotics.lib.mathematics.twodim.trajectory.view.TimedView
import org.ghrobotics.lib.mathematics.units.FeetPerSecond
import org.ghrobotics.lib.utils.BooleanSource
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.l
import org.ghrobotics.lib.utils.observabletype.*
import org.ghrobotics.lib.utils.r
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.Kinematics
import org.ghrobotics.robot.Localization
import kotlin.math.sign

class FollowTrajectoryCommand(val trajectory: Source<Trajectory<TimedState<Pose2dWithCurvature>>>,
                              pathMirrored: BooleanSource = Source(false)) : org.ghrobotics.lib.commands.Command(DriveSubsystem) {

    constructor(trajectory: Trajectory<TimedState<Pose2dWithCurvature>>,
                pathMirrored: BooleanSource = Source(false)) : this(Source(trajectory), pathMirrored)

    private val trajectoryFollower: TrajectoryFollower

    private val markerLocations = mutableListOf<Marker>()
    private val markers = mutableListOf<MarkerInternal>()

    private val trajectoryUsed: Trajectory<TimedState<Pose2dWithCurvature>>

    private var lastVelocity = 0.0 to 0.0

    init {
        // Update the frequency of the command to the follower
        executeFrequency = 100 // Hz

        // Add markers
        markers.clear()

        trajectoryUsed = if (pathMirrored.value) trajectory.value else trajectory.value.mirrorTimed()

        // Iterate through the trajectory and add a data point every 50 ms.
        val iterator = TrajectoryIterator(TimedView(trajectory.value))
        val dataArray = arrayListOf<TrajectorySamplePoint<TimedState<Pose2dWithCurvature>>>()

        while (!iterator.isDone) {
            dataArray.add(iterator.advance(0.05))
        }
        markerLocations.forEach { marker ->
            val condition = marker.condition as ObservableVariable<Boolean>
            condition.value = false // make sure its false

            val usedLocation = marker.location.value
            markers.add(MarkerInternal(dataArray.minBy { usedLocation.distance(it.state.state.translation) }!!.state.t, condition))
        }

        // Initialize path follower
        trajectoryFollower = NonLinearController(trajectoryUsed, Constants.kDriveBeta, Constants.kDriveZeta)

        _finishCondition += UpdatableObservableValue { trajectoryFollower.isFinished }
    }

    fun addMarkerAt(location: Translation2d) = addMarkerAt(Source(location))
    fun addMarkerAt(location: Source<Translation2d>) = Marker(location, ObservableVariable(false)).also { markerLocations.add(it) }

    private fun updateDashboard() {
        pathX = trajectoryFollower.pose.translation.x
        pathY = trajectoryFollower.pose.translation.y
        pathHdg = trajectoryFollower.pose.rotation.radians

        lookaheadX = pathX
        lookaheadY = pathY
    }

    override suspend fun execute() {
        val position = Localization.robotPosition
        val kinematics = trajectoryFollower.getSteering(position)
        val output = Kinematics.inverseKinematics(kinematics)

        val lVelocitySTU = FeetPerSecond(output.l).STU.toDouble()
        val rVelocitySTU = FeetPerSecond(output.r).STU.toDouble()

        val lAccelerationSTU = FeetPerSecond((output.l - lastVelocity.l) * executeFrequency).STU / 1000.0 // Why CTRE
        val rAccelerationSTU = FeetPerSecond((output.r - lastVelocity.r) * executeFrequency).STU / 1000.0 // Why CTRE

        DriveSubsystem.setTrajectoryVelocity(Output(
                lSetpoint = lVelocitySTU, lAdditiveFF = Constants.kADrive * lAccelerationSTU + Constants.kSDrive * sign(lVelocitySTU),
                rSetpoint = rVelocitySTU, rAdditiveFF = Constants.kADrive * rAccelerationSTU + Constants.kSDrive * sign(rVelocitySTU)
        ))

        updateDashboard()


        // Update marker states
        val followerStateTime = trajectoryFollower.point.state.t
        markers.forEach { it.condition.value = followerStateTime > it.t }

        lastVelocity = output
    }

    override suspend fun dispose() {
        DriveSubsystem.set(controlMode = ControlMode.PercentOutput, leftOutput = 0.0, rightOutput = 0.0)
        println("[Trajectory Follower] " +
                "Norm of Translational Error: " +
                "${(Localization.robotPosition.translation - trajectoryUsed.lastState.state.translation).norm}")
        println("[Trajectory Follower]" +
                "Rotation Error: " +
                "${(Localization.robotPosition.rotation - trajectoryUsed.lastState.state.rotation).degrees} degrees.")

    }

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

    class Marker(val location: Source<Translation2d>, val condition: ObservableValue<Boolean>)
    private class MarkerInternal(val t: Double, val condition: ObservableVariable<Boolean>)

    class Output(val lSetpoint: Double, val rSetpoint: Double, val lAdditiveFF: Double, val rAdditiveFF: Double)
}
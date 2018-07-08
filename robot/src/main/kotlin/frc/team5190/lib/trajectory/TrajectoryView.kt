package frc.team5190.lib.trajectory

import frc.team5190.lib.geometry.interfaces.State

interface TrajectoryView<S : State<S>> {

    fun sample(interpolant: Double): TrajectorySamplePoint<S>

    val firstInterpolant: Double
    val lastInterpolant: Double

    val trajectory: Trajectory<S>
}

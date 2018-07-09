@file:Suppress("unused")

package frc.team5190.lib.trajectory

import frc.team5190.lib.geometry.interfaces.State

class TrajectoryIterator<S : State<S>>(private val view: TrajectoryView<S>) {

    private var progress = 0.0
    private var sample: TrajectorySamplePoint<S>


    val isDone: Boolean
        get() = remainingProgress == 0.0

    private val remainingProgress: Double
        get() = Math.max(0.0, view.lastInterpolant - progress)

    val state: S
        get() = sample.state

    init {
        sample = view.sample(view.firstInterpolant)
        progress = view.firstInterpolant
    }

    fun advance(additional_progress: Double): TrajectorySamplePoint<S> {
        progress = Math.max(view.firstInterpolant,
                Math.min(view.lastInterpolant, progress + additional_progress))
        sample = view.sample(progress)

        return sample
    }

    fun preview(additional_progress: Double): TrajectorySamplePoint<S> {
        val progress = Math.max(view.firstInterpolant,
                Math.min(view.lastInterpolant, this.progress + additional_progress))
        return view.sample(progress)
    }

    fun trajectory(): Trajectory<S> {
        return view.trajectory
    }
}
package frc.team5190.lib.trajectory

import frc.team5190.lib.geometry.interfaces.State

class TrajectorySamplePoint<S : State<S>> {

    val state: S
    private val indexFloor: Int
    private val indexCeil: Int

    constructor(state: S, indexFloor: Int, indexCeil: Int) {
        this.state = state
        this.indexFloor = indexFloor
        this.indexCeil = indexCeil
    }

    constructor(point: TrajectoryPoint<S>) {
        state = point.state()
        indexCeil = point.index()
        indexFloor = indexCeil
    }
}

package org.ghrobotics.robot.auto

import org.ghrobotics.lib.mathematics.units.second


fun main(args: Array<String>) {
    val trajectory = Trajectories.leftStartToFarScale
    val iterator = trajectory.iterator()

    while (!iterator.isDone) {
        val pt = iterator.advance(0.2.second)
        System.out.printf(
            "dt: %3.3f, x: %3.3f, y: %3.3f, velocity: %3.3f, acceleration: %3.3f%n",
            0.2, pt.state.state.pose.translation.x.value, pt.state.state.pose.translation.y.value,
            pt.state.velocity.value, pt.state.acceleration.value
        )
    }
}


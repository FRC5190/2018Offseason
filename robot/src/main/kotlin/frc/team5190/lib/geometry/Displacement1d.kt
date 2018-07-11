/*
 * FRC Team 5190
 * Green Hope Falcons
 */

/*
 * Some implementations and algorithms borrowed from:
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 */

@file:Suppress("unused", "EqualsOrHashCode")

package frc.team5190.lib.geometry

import frc.team5190.lib.extensions.epsilonEquals
import frc.team5190.lib.geometry.interfaces.State
import frc.team5190.lib.types.Interpolable
import java.text.DecimalFormat

class Displacement1d : State<Displacement1d> {

    private val displacement: Double

    constructor() {
        displacement = 0.0
    }

    constructor(displacement: Double) {
        this.displacement = displacement
    }

    fun x(): Double {
        return displacement
    }

    override fun interpolate(other: Displacement1d, x: Double): Displacement1d {
        return Displacement1d(Interpolable.interpolate(displacement, other.displacement, x))
    }

    override fun distance(other: Displacement1d): Double {
        return Math.abs(x() - other.x())
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is Displacement1d) false else x() epsilonEquals other.x()
    }

    override fun toString(): String {
        val fmt = DecimalFormat("#0.000")
        return fmt.format("(" + x() + ")")
    }

    override fun toCSV(): String {
        val fmt = DecimalFormat("#0.000")
        return fmt.format(x())
    }
}

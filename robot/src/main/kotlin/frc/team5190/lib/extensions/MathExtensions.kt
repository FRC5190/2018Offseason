package frc.team5190.lib.extensions

import frc.team5190.lib.math.EPSILON
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import kotlin.math.PI
import kotlin.math.absoluteValue

infix fun Double.epsilonEquals(other: Double): Boolean {
    return (this - other).absoluteValue < EPSILON
}

infix fun Double.cos(other: Double): Double {
    return this * Math.cos(other)
}

infix fun Double.sin(other: Double): Double {
    return this * Math.sin(other)
}

fun Double.enforceBounds(): Double {
    var x = this
    while (x >= PI) x -= (2 * PI)
    while (x < -PI) x += (2 * PI)
    return x
}


typealias Matrix = Array2DRowRealMatrix

operator fun Matrix.plus(other: Matrix) = this.add(other)!!
operator fun Matrix.minus(other: Matrix) = this.subtract(other)!!
operator fun Matrix.times(other: Matrix) = this.multiply(other)!!


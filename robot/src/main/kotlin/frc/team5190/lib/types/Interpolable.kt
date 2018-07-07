package frc.team5190.lib.types

interface Interpolable<T> {
    fun interpolate(upperVal: T, interpolatePoint: Double): T

    companion object {
        fun interpolate(a: Double, b: Double, x: Double): Double {
            var x = x
            x = x.coerceIn(0.0, 1.0)
            return a + (b - a) * x
        }
    }
}
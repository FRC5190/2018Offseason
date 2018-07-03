package frc.team5190.lib.types

interface Interpolable<T> {
    fun interpolate(upperVal: T, interpolatePoint: Double): T
}
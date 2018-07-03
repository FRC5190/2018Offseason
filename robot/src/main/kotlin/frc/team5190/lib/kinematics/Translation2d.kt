@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package frc.team5190.lib.kinematics

import frc.team5190.lib.extensions.Vector2d
import frc.team5190.lib.extensions.plus
import frc.team5190.lib.extensions.rotateVector2d
import frc.team5190.lib.types.Interpolable

class Translation2d : Interpolable<Translation2d> {

    var position = Vector2d(0.0, 0.0)

    var x: Double
        get() = position.x
        set(value) {
            position = Vector2d(value, y)
        }

    var y: Double
        get() = position.y
        set(value) {
            position = Vector2d(x, value)
        }

    constructor()

    constructor(x: Double, y: Double) {
        position = Vector2d(x, y)
    }

    constructor(toCopy: Translation2d) {
        position = toCopy.position
    }

    constructor(vector: Vector2d) {
        position = vector
    }

    val norm: Double
        get() = position.norm

    val normalized: Vector2d
        get() = position.normalize()

    infix fun translateBy(other: Translation2d): Translation2d = translateBy(other.position)
    infix fun translateBy(other: Vector2d): Translation2d =
            Translation2d(position + other)

    infix fun rotateByOrigin(rotation: Rotation2d): Translation2d =
            Translation2d(rotateVector2d(position, rotation.rotation))

    fun inverse(): Translation2d = Translation2d(position.negate())

    override fun interpolate(upperVal: Translation2d, interpolatePoint: Double): Translation2d =
            when {
                (interpolatePoint <= 0) -> Translation2d(this)
                (interpolatePoint >= 1) -> Translation2d(upperVal)
                else -> extrapolate(upperVal, interpolatePoint)
            }

    fun extrapolate(slopePoint: Translation2d, extrapolatePoint: Double): Translation2d =
            Translation2d(
                    extrapolatePoint * (slopePoint.x - position.x) + position.x,
                    extrapolatePoint * (slopePoint.y - position.y) + position.y
            )

    override fun toString(): String =
            "[x: ${"%.3f".format(position.x)}, y: ${"%.3f".format(position.y)}]"
}


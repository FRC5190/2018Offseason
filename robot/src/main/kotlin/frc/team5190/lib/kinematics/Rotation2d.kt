@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package frc.team5190.lib.kinematics

import frc.team5190.lib.extensions.Vector2d
import frc.team5190.lib.extensions.atan2
import frc.team5190.lib.extensions.rotateVector2d
import frc.team5190.lib.math.EPSILON
import frc.team5190.lib.types.Interpolable

class Rotation2d : Interpolable<Rotation2d> {

    companion object {
        fun createFromRadians(angleRadians: Double): Rotation2d =
                Rotation2d(Math.cos(angleRadians), Math.sin(angleRadians))

        fun createFromDegrees(angleDegrees: Double): Rotation2d =
                createFromRadians(Math.toRadians(angleDegrees))
    }


    var rotation: Vector2d = Vector2d(1.0, 0.0)

    constructor()

    constructor(cos: Double, sin: Double) {
        rotation = Vector2d(cos, sin).normalize()
    }

    constructor(toSet: Rotation2d) {
        rotation = toSet.rotation.normalize()
    }

    constructor(toSetVector: Vector2d) {
        rotation = toSetVector.normalize()
    }

    var cos: Double
        get() = rotation.x
        set(value) {
            rotation = Vector2d(value, rotation.x)
        }

    var sin: Double
        get() = rotation.y
        set(value) {
            rotation = Vector2d(rotation.y, value)
        }

    val tan: Double
        get() =
            when {
                rotation.x > EPSILON -> rotation.y / rotation.x
                rotation.y >= 0.0 -> Double.POSITIVE_INFINITY
                else -> Double.NEGATIVE_INFINITY
            }

    var radians: Double
        get() = rotation.atan2
        set(value) {
            rotation = createFromRadians(value).rotation
        }

    var degrees: Double
        get() = Math.toDegrees(radians)
        set(value) {
            rotation = createFromDegrees(value).rotation
        }

    var theta: Double
        get() = radians
        set(value) {
            radians = value
        }

    infix fun rotateBy(toRotateBy: Rotation2d): Rotation2d {
        val rotated = rotateVector2d(rotation, toRotateBy.rotation)
        return Rotation2d(rotated)
    }


    val inverse: Rotation2d
        get() = Rotation2d(cos, -sin)

    override fun interpolate(upperVal: Rotation2d, interpolatePoint: Double): Rotation2d {
        return when {
            interpolatePoint <= 0 -> Rotation2d(this)
            interpolatePoint >= 1 -> Rotation2d(upperVal)
            else -> this.rotateBy(
                    Rotation2d.createFromRadians(inverse.rotateBy(upperVal).radians * interpolatePoint)
            )
        }
    }

    override fun toString(): String {
        return "cos: ${"%.3f".format(cos)} sin: ${"%.3f".format(sin)} deg: ${"%.3f".format(degrees)}"
    }
}

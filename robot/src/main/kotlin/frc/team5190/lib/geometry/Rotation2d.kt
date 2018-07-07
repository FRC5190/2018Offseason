/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package frc.team5190.lib.geometry

import frc.team5190.lib.extensions.Vector2d
import frc.team5190.lib.extensions.atan2
import frc.team5190.lib.extensions.rotateVector2d
import frc.team5190.lib.geometry.interfaces.IRotation2d
import frc.team5190.lib.math.EPSILON

class Rotation2d : IRotation2d<Rotation2d> {

    companion object {
        fun createFromRadians(angleRadians: Double): Rotation2d =
                Rotation2d(Math.cos(angleRadians), Math.sin(angleRadians))

        fun createFromDegrees(angleDegrees: Double): Rotation2d =
                createFromRadians(Math.toRadians(angleDegrees))
    }


    override val rotation
        get() = this

    var rotationVector: Vector2d = Vector2d(1.0, 0.0)

    var cos: Double
        get() = rotationVector.x
        set(value) {
            rotationVector = Vector2d(value, rotationVector.x)
        }

    var sin: Double
        get() = rotationVector.y
        set(value) {
            rotationVector = Vector2d(rotationVector.y, value)
        }

    val tan: Double
        get() =
            when {
                rotationVector.x > EPSILON -> rotationVector.y / rotationVector.x
                rotationVector.y >= 0.0 -> Double.POSITIVE_INFINITY
                else -> Double.NEGATIVE_INFINITY
            }

    var radians: Double
        get() = rotationVector.atan2
        set(value) {
            rotationVector = createFromRadians(value).rotationVector
        }

    var degrees: Double
        get() = Math.toDegrees(radians)
        set(value) {
            rotationVector = createFromDegrees(value).rotationVector
        }

    var theta: Double
        get() = radians
        set(value) {
            radians = value
        }

    val inverse: Rotation2d
        get() = Rotation2d(cos, -sin)

    val normal: Rotation2d
        get() = Rotation2d(-sin, cos)

    constructor()

    constructor(cos: Double, sin: Double) {
        rotationVector = Vector2d(cos, sin).normalize()
    }

    constructor(toSet: Rotation2d) {
        rotationVector = toSet.rotationVector.normalize()
    }

    constructor(toSetVector: Vector2d) {
        rotationVector = toSetVector.normalize()
    }

    fun rotateBy(toRotateBy: Rotation2d): Rotation2d {
        val rotated = rotateVector2d(rotationVector, toRotateBy.rotationVector)
        return Rotation2d(rotated)
    }


    override fun distance(other: Rotation2d) = inverse.rotateBy(other).radians

    override fun interpolate(upperVal: Rotation2d, interpolatePoint: Double): Rotation2d {
        return when {
            interpolatePoint <= 0 -> Rotation2d(this)
            interpolatePoint >= 1 -> Rotation2d(upperVal)
            else -> this.rotateBy(
                    Rotation2d.createFromRadians(inverse.rotateBy(upperVal).radians * interpolatePoint)
            )
        }
    }
}

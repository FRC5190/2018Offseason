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
import frc.team5190.lib.extensions.plus
import frc.team5190.lib.extensions.rotateVector2d
import frc.team5190.lib.geometry.interfaces.ITranslation2d

class Translation2d : ITranslation2d<Translation2d> {

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

    val norm: Double
        get() = position.norm

    val normalized: Vector2d
        get() = position.normalize()


    val inverse: Translation2d
        get() = Translation2d(position.negate())

    override val translation: Translation2d
        get() = this


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


    fun translateBy(other: Translation2d): Translation2d = translateBy(other.position)
    fun translateBy(other: Vector2d): Translation2d = Translation2d(position + other)

    fun extrapolate(slopePoint: Translation2d, extrapolatePoint: Double): Translation2d =
            Translation2d(
                    extrapolatePoint * (slopePoint.x - position.x) + position.x,
                    extrapolatePoint * (slopePoint.y - position.y) + position.y
            )

    fun rotateBy(rotation: Rotation2d): Translation2d = Translation2d(rotateVector2d(position, rotation.rotationVector))

    override fun interpolate(upperVal: Translation2d, interpolatePoint: Double): Translation2d =
            when {
                (interpolatePoint <= 0) -> Translation2d(this)
                (interpolatePoint >= 1) -> Translation2d(upperVal)
                else -> extrapolate(upperVal, interpolatePoint)
            }

    override fun distance(other: Translation2d) = inverse.translateBy(other).norm
}


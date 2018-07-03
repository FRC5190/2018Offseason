@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package frc.team5190.lib.kinematics

import frc.team5190.lib.extensions.rotateVector2d
import frc.team5190.lib.math.EPSILON
import frc.team5190.lib.types.Interpolable
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

class Pose2d : Interpolable<Pose2d> {

    companion object {
        fun fromTranslation(translation: Translation2d): Pose2d {
            return Pose2d(translation, Rotation2d())
        }

        fun fromRotation(rotation: Rotation2d): Pose2d {
            return Pose2d(Translation2d(), rotation)
        }

        fun fromDelta(twist: Twist2d): Pose2d {
            val sinTheta: Double = Math.sin(twist.dtheta)
            val cosTheta: Double = Math.cos(twist.dtheta)
            val sin: Double
            val cos: Double

            if (Math.abs(twist.dtheta) < EPSILON) {
                sin = 1.0 - (1.0 / 6.0) * Math.pow(twist.dtheta, 2.0)
                cos = 0.5 * twist.dtheta
            } else {
                sin = sinTheta / twist.dtheta
                cos = (1.0 - cosTheta) / twist.dtheta
            }

            val rotated = rotateVector2d(source = Vector2D(twist.dx, twist.dy), rotation = Vector2D(sin, cos))
            return Pose2d(Translation2d(rotated.x, rotated.y), Rotation2d(cosTheta, sinTheta))
        }
    }

    var translation: Translation2d = Translation2d()
    var rotation: Rotation2d = Rotation2d()
    var frameOfReference = FramesOfReference.FIELD

    val inverse: Pose2d
        get() =
            Pose2d(translation.inverse.rotateByOrigin(rotation.inverse), rotation.inverse)

    val x: Double
        get() = translation.x
    val y: Double
        get() = translation.y

    val cos: Double
        get() = rotation.cos
    val sin: Double
        get() = rotation.sin

    val theta: Double
        get() = rotation.theta

    constructor()

    constructor(translation: Translation2d, rotation: Rotation2d) {
        this.translation = translation
        this.rotation = rotation
    }

    constructor(toCopy: Pose2d) {
        translation = Translation2d(toCopy.translation)
        rotation = Rotation2d(toCopy.rotation)
    }

    fun transformBy(other: Pose2d): Pose2d {
        return Pose2d(
                translation.translateBy(other.translation.rotateByOrigin(rotation)),
                rotation.rotateBy(other.rotation)
        )
    }

    fun convertTo(other: FramesOfReference): Pose2d {
        val rotation = other.orientationRelativeToField.inverse.rotateBy(frameOfReference.orientationRelativeToField)
        val translation = other.originRelativeToField.inverse.translateBy(frameOfReference.originRelativeToField)

        return Pose2d(translation, rotation).transformBy(this)
    }

    override fun interpolate(upperVal: Pose2d, interpolatePoint: Double): Pose2d =
            when {
                (interpolatePoint <= 0) -> Pose2d(this)
                (interpolatePoint >= 1) -> Pose2d(upperVal)
                else -> Pose2d(
                        translation.interpolate(upperVal.translation, interpolatePoint),
                        rotation.interpolate(upperVal.rotation, interpolatePoint)
                )
            }

}

/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package frc.team5190.lib.geometry

import frc.team5190.lib.extensions.rotateVector2d
import frc.team5190.lib.geometry.interfaces.IPose2d
import frc.team5190.lib.kinematics.FrameOfReference
import frc.team5190.lib.math.EPSILON
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D


class Pose2d : IPose2d<Pose2d> {

    companion object {
        fun fromTranslation(translation: Translation2d): Pose2d {
            return Pose2d(translation, Rotation2d())
        }

        fun fromRotation(rotation: Rotation2d): Pose2d {
            return Pose2d(Translation2d(), rotation)
        }

        fun fromTwist(twist: Twist2d): Pose2d {
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

    override val pose
        get() = this

    override var translation: Translation2d = Translation2d()
    override var rotation: Rotation2d = Rotation2d()

    var frameOfReference = FrameOfReference.FIELD


    val inverse: Pose2d
        get() = Pose2d(translation.inverse.rotateBy(rotation.inverse), rotation.inverse)

    val normal: Pose2d
        get() = Pose2d(translation, rotation.normal)


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

    constructor(x: Double, y: Double, rotation: Rotation2d) {
        this.translation = Translation2d(x, y)
        this.rotation = rotation
    }

    constructor(toCopy: Pose2d) {
        translation = Translation2d(toCopy.translation)
        rotation = Rotation2d(toCopy.rotation)
    }



    fun convertToFOR(other: FrameOfReference): Pose2d {
        val rotation = other.orientationRelativeToField.inverse.rotateBy(frameOfReference.orientationRelativeToField)
        val translation = other.originRelativeToField.inverse.translateBy(frameOfReference.originRelativeToField)

        return Pose2d(translation, rotation).transformBy(this)
    }


    override fun mirror() = Pose2d(translation.x, 27.0 - translation.y, rotation.inverse)
    override fun distance(other: Pose2d) = Twist2d.fromPose(this.inverse.transformBy(other)).norm


    override fun transformBy(other: Pose2d): Pose2d {
        return Pose2d(
                translation.translateBy(other.translation.rotateBy(rotation)),
                rotation.rotateBy(other.rotation)
        )
    }


    override fun interpolate(upperVal: Pose2d, interpolatePoint: Double): Pose2d =
            when {
                interpolatePoint <= 0 -> {
                    Pose2d(this)
                }
                interpolatePoint >= 1 -> {
                    Pose2d(upperVal)
                }
                else -> {
                    val twist = Twist2d.fromPose(inverse.transformBy(upperVal))
                    transformBy(Pose2d.fromTwist(twist.scaled(interpolatePoint)))
                }
            }

}

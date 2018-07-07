/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */


package frc.team5190.lib.geometry

import com.sun.javafx.binding.StringFormatter
import frc.team5190.lib.geometry.interfaces.ICurvature
import frc.team5190.lib.geometry.interfaces.IPose2d
import frc.team5190.lib.geometry.interfaces.State
import frc.team5190.lib.types.Interpolable


class Pose2dWithCurvature(override val pose: Pose2d = Pose2d(), override val curvature: Double = 0.0, override val dkds: Double = 0.0)
    : IPose2d<Pose2dWithCurvature>, ICurvature<Pose2dWithCurvature> {

    override fun transformBy(transform: Pose2d) = Pose2dWithCurvature(
            pose.transformBy(transform), curvature, dkds
    )

    override fun mirror() = Pose2dWithCurvature(
            pose.mirror(), curvature, dkds
    )


    override val rotation: Rotation2d
        get() = pose.rotation

    override fun distance(other: Pose2dWithCurvature) = pose.distance(other.pose)


    override fun interpolate(upperVal: Pose2dWithCurvature, interpolatePoint: Double): Pose2dWithCurvature =
            Pose2dWithCurvature(pose.interpolate(upperVal.pose, interpolatePoint),
                    Interpolable.interpolate(curvature, upperVal.dkds, interpolatePoint),
                    Interpolable.interpolate(dkds, upperVal.dkds, interpolatePoint))

    override val translation: Translation2d
        get() = pose.translation

    override fun toString(): String {
        return StringFormatter.format("X: %3f, Y: %3f, Theta: %3f", pose.x, pose.y, pose.rotation.degrees).valueSafe
    }


}
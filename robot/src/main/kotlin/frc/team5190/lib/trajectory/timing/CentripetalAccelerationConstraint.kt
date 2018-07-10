@file:Suppress("unused")

package frc.team5190.lib.trajectory.timing

import frc.team5190.lib.geometry.Pose2dWithCurvature

class CentripetalAccelerationConstraint(private val mMaxCentripetalAccel: Double) : TimingConstraint<Pose2dWithCurvature> {

    override fun getMaxVelocity(state: Pose2dWithCurvature): Double {
        return Math.sqrt(Math.abs(mMaxCentripetalAccel / state.curvature))
    }

    override fun getMinMaxAcceleration(state: Pose2dWithCurvature, velocity: Double): TimingConstraint.MinMaxAcceleration {
        return TimingConstraint.MinMaxAcceleration.kNoLimits
    }
}

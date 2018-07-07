package frc.team254.lib.trajectory.timing;

import frc.team5190.lib.geometry.Translation2d;
import frc.team5190.lib.geometry.interfaces.ITranslation2d;

public class VelocityLimitRegionConstraint<S extends ITranslation2d<S>> implements TimingConstraint<S> {
    protected final Translation2d min_corner_;
    protected final Translation2d max_corner_;
    protected final double velocity_limit_;

    public VelocityLimitRegionConstraint(Translation2d min_corner, Translation2d max_corner, double velocity_limit) {
        min_corner_ = min_corner;
        max_corner_ = max_corner;
        velocity_limit_ = velocity_limit;
    }

    @Override
    public double getMaxVelocity(S state) {
        final Translation2d translation = state.getTranslation();
        if (translation.getX() <= max_corner_.getX() && translation.getX() >= min_corner_.getX() &&
                translation.getY() <= max_corner_.getY() && translation.getY() >= min_corner_.getY()) {
            return velocity_limit_;
        }
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public TimingConstraint.MinMaxAcceleration getMinMaxAcceleration(S state,
                                                                     double velocity) {
        return MinMaxAcceleration.kNoLimits;
    }

}

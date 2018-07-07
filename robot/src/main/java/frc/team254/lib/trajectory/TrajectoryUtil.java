package frc.team254.lib.trajectory;

import frc.team254.lib.spline.QuinticHermiteSpline;
import frc.team254.lib.spline.Spline;
import frc.team254.lib.spline.SplineGenerator;
import frc.team254.lib.trajectory.timing.TimedState;
import frc.team254.lib.util.Util;
import frc.team5190.lib.geometry.Pose2d;
import frc.team5190.lib.geometry.Pose2dWithCurvature;
import frc.team5190.lib.geometry.interfaces.IPose2d;
import frc.team5190.lib.geometry.interfaces.State;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryUtil {
    public static <S extends IPose2d<S>> Trajectory<S> mirror(final Trajectory<S> trajectory) {
        List<S> waypoints = new ArrayList<>(trajectory.length());
        for (int i = 0; i < trajectory.length(); ++i) {
            waypoints.add(trajectory.getState(i).mirror());
        }
        return new Trajectory<>(waypoints);
    }

    public static <S extends IPose2d<S>> Trajectory<TimedState<S>> mirrorTimed(final Trajectory<TimedState<S>> trajectory) {
        List<TimedState<S>> waypoints = new ArrayList<>(trajectory.length());
        for (int i = 0; i < trajectory.length(); ++i) {
            TimedState<S> timed_state = trajectory.getState(i);
            waypoints.add(new TimedState<S>(timed_state.state().mirror(), timed_state.t(), timed_state.velocity(), timed_state.acceleration()));
        }
        return new Trajectory<>(waypoints);
    }

    public static <S extends IPose2d<S>> Trajectory<S> transform(final Trajectory<S> trajectory, Pose2d transform) {
        List<S> waypoints = new ArrayList<>(trajectory.length());
        for (int i = 0; i < trajectory.length(); ++i) {
            waypoints.add(trajectory.getState(i).transformBy(transform));
        }
        return new Trajectory<>(waypoints);
    }

    /**
     * Creates a Trajectory by sampling a TrajectoryView at a regular interval.
     *
     * @param trajectory_view
     * @param interval
     * @return
     */
    public static <S extends State<S>> Trajectory<S> resample(
            final TrajectoryView<S> trajectory_view, double interval) {
        if (interval <= Util.kEpsilon) {
            return new Trajectory<S>();
        }
        final int num_states = (int) Math
                .ceil((trajectory_view.last_interpolant() - trajectory_view.first_interpolant()) / interval);
        ArrayList<S> states = new ArrayList<S>(num_states);

        for (int i = 0; i < num_states; ++i) {
            states.add(trajectory_view.sample(i * interval + trajectory_view.first_interpolant()).state());
        }
        return new Trajectory<S>(states);
    }

    public static Trajectory<Pose2dWithCurvature> trajectoryFromSplineWaypoints(final List<Pose2d> waypoints, double
            maxDx, double maxDy, double maxDTheta) {
        List<QuinticHermiteSpline> splines = new ArrayList<>(waypoints.size() - 1);
        for (int i = 1; i < waypoints.size(); ++i) {
            splines.add(new QuinticHermiteSpline(waypoints.get(i - 1), waypoints.get(i)));
        }
        QuinticHermiteSpline.optimizeSpline(splines);
        return trajectoryFromSplines(splines, maxDx, maxDy, maxDTheta);
    }

    public static Trajectory<Pose2dWithCurvature> trajectoryFromSplines(final List<? extends Spline> splines, double
            maxDx,
                                                                        double maxDy, double maxDTheta) {
        return new Trajectory<>(SplineGenerator.parameterizeSplines(splines, maxDx, maxDy,
                maxDTheta));
    }

    ;
}

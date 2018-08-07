/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.math.trajectory.followers

import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Pose2dWithCurvature
import frc.team5190.lib.math.trajectory.Trajectory
import frc.team5190.lib.math.trajectory.TrajectoryIterator
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.view.TimedView
import frc.team5190.robot.auto.Trajectories
import org.junit.Test

class NonLinearReferenceControllerTest {

    private lateinit var trajectoryFollower: TrajectoryFollower

    @Test
    fun testTrajectoryFollower() {
        val name = "Left Start to Far Scale"
        val trajectory: Trajectory<TimedState<Pose2dWithCurvature>> = Trajectories[name]
        val iterator = TrajectoryIterator(TimedView(trajectory))
        trajectoryFollower = NonLinearReferenceController(trajectory)

        var totalpose = trajectory.firstState.state.pose

        var time = 0.0
        val dt = 0.02

        while (!iterator.isDone) {
            val pt = iterator.advance(dt)
            val output = trajectoryFollower.getSteering(totalpose, time.toLong())
            time += dt * 1.0e+9

//            println (pt.state.state.curvature)

            assert(if (trajectory.firstState.acceleration > 0) output.dx >= 0 else output.dx <= 0)

            totalpose = totalpose.transformBy(Pose2d.fromTwist(output.scaled(dt)))
        }

        assert((trajectory.lastState.state.translation - totalpose.translation).norm.also {
            println("Norm of Translational Error: $it")
        } < 0.50)
        assert((trajectory.lastState.state.rotation - totalpose.rotation).degrees.also {
            println("Rotational Error: $it degrees")
        } < 5.0)
    }
}
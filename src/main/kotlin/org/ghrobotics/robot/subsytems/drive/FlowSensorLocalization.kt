package org.ghrobotics.robot.subsytems.drive

import org.ghrobotics.lib.localization.Localization
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Twist2d
import org.ghrobotics.lib.mathematics.units.Rotation2d
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.Robot

class FlowSensorLocalization(
    robotHeading: Source<Rotation2d>,
    val flowSensor: Source<Translation2d>
) : Localization(robotHeading, Robot.coroutineContext) {

    private var previousFlowSensorPosition = Translation2d()

    override fun resetInternal(newPosition: Pose2d) {
        super.resetInternal(newPosition)
        previousFlowSensorPosition = flowSensor()
    }

    override fun update(deltaHeading: Rotation2d): Pose2d {
        val newFlowSensorPosition = flowSensor()
        val delta = newFlowSensorPosition - previousFlowSensorPosition

        previousFlowSensorPosition = newFlowSensorPosition

        return Twist2d(delta.x, delta.y, deltaHeading).asPose
    }
}
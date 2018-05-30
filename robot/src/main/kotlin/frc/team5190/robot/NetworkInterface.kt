package frc.team5190.robot

import edu.wpi.first.networktables.NetworkTableInstance
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.robot.sensors.Pigeon
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

object NetworkInterface {
    init {
        launch {
            val plotterInstance = NetworkTableInstance.getDefault().getTable("PosePlotter")

            val robotX = plotterInstance.getEntry("Robot X")
            val robotY = plotterInstance.getEntry("Robot Y")
            val robotHdg = plotterInstance.getEntry("Robot Heading")

            val pathX = plotterInstance.getEntry("Path X")
            val pathY = plotterInstance.getEntry("Path Y")
            val pathHdg = plotterInstance.getEntry("Path Heading")

            val lookaheadX = plotterInstance.getEntry("Lookahead X")
            val lookaheadY = plotterInstance.getEntry("Lookahead Y")

            while (true) {
                robotX.setDouble(Localization.robotPosition.x)
                robotY.setDouble(Localization.robotPosition.y)
                robotHdg.setDouble(Math.toRadians(Pigeon.correctedAngle))

                pathX.setDouble(FollowPathCommand.pathX)
                pathY.setDouble(FollowPathCommand.pathY)
                pathHdg.setDouble(FollowPathCommand.pathHdg)

                lookaheadX.setDouble(FollowPathCommand.lookaheadX)
                lookaheadY.setDouble(FollowPathCommand.lookaheadY)

                delay(10, TimeUnit.MILLISECONDS)
            }
        }
    }
}
package frc.team5190.robot

import edu.wpi.first.wpilibj.Notifier
import frc.team5190.lib.extensions.enforceBounds
import frc.team5190.lib.extensions.epsilonEquals
import frc.team5190.lib.kinematics.Pose2d
import frc.team5190.lib.kinematics.Rotation2d
import frc.team5190.lib.kinematics.Translation2d
import frc.team5190.lib.kinematics.Twist2d
import frc.team5190.lib.math.Pose2D
import frc.team5190.lib.units.Distance
import frc.team5190.lib.units.NativeUnits
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.sensors.NavX
import jaci.pathfinder.Pathfinder
import javafx.geometry.Pos
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import kotlin.math.cos
import kotlin.math.sin

object Localization {

    private const val ANGLE_FILTER = 0.9999

    private val loc = Object()

    var robotPosition = Pose2d()
        private set

    private var prevL: Distance = NativeUnits(0)
    private var prevR: Distance = NativeUnits(0)
    private var prevA = 0.0

    init {
        reset()
        Notifier(this::run).startPeriodic(0.05)
    }

    private fun reset(pose: Pose2d = Pose2d(Translation2d(), Rotation2d())) {
        synchronized(loc) {
            robotPosition = pose
            prevL = DriveSubsystem.leftPosition
            prevR = DriveSubsystem.rightPosition
            prevA = Math.toRadians(NavX.correctedAngle)
        }
    }

    fun reset(translation2d: Translation2d) = reset(Pose2d(translation2d, Rotation2d()))

    private fun run() {
        synchronized(loc) {
            val posL = DriveSubsystem.leftPosition
            val posR = DriveSubsystem.rightPosition

            val angA = Math.toRadians(Pathfinder.boundHalfDegrees(
                    (ANGLE_FILTER * NavX.correctedAngle) + ((1 - ANGLE_FILTER) * prevA)))

            val deltaL = posL - prevL
            val deltaR = posR - prevR
            val deltaA = (angA - prevA).enforceBounds()

            val distance = ((deltaL + deltaR) / 2.0).FT.value
            val delta = Twist2d(dx = distance, dy = 0.0, dtheta = deltaA)

            robotPosition = robotPosition.transformBy(Pose2d.fromDelta(delta))
        }
    }
}

/*
 * FRC Team 5190
 * Green Hope Falcons
 */


package frc.team5190.robot.auto

import edu.wpi.first.wpilibj.Notifier
import frc.team5190.lib.extensions.enforceBounds
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.geometry.Translation2d
import frc.team5190.lib.units.Distance
import frc.team5190.lib.units.NativeUnits
import frc.team5190.robot.Kinematics
import frc.team5190.robot.subsytems.drive.DriveSubsystem
import frc.team5190.robot.sensors.NavX

object Localization {

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

    fun reset(pose: Pose2d = Pose2d(Translation2d(), Rotation2d())) {
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

            val angA = Math.toRadians(NavX.correctedAngle)

            val deltaL = posL - prevL
            val deltaR = posR - prevR
            val deltaA = (angA - prevA).enforceBounds()

            val kinematics = Kinematics.forwardKinematics(deltaL.FT, deltaR.FT, deltaA)
            robotPosition = robotPosition.transformBy(Pose2d.fromTwist(kinematics))
        }
    }
}

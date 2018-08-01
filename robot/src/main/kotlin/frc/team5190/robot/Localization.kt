/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.wpilibj.Notifier
import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Rotation2d
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.NativeUnits
import frc.team5190.robot.sensors.AHRS
import frc.team5190.robot.subsytems.drive.DriveSubsystem

object Localization {

    private val loc = Object()

    var robotPosition = Pose2d()
        private set

    private var prevL: Distance = NativeUnits(0)
    private var prevR: Distance = NativeUnits(0)
    private var prevA = Rotation2d()

    init {
        reset()
        Notifier(::run).startPeriodic(0.01)
    }

    fun reset(pose: Pose2d = Pose2d(Translation2d(), Rotation2d())) {
        synchronized(loc) {
            robotPosition = pose
            prevL = DriveSubsystem.leftPosition
            prevR = DriveSubsystem.rightPosition
            prevA = AHRS.correctedAngle
        }
    }

    private fun run() {
        synchronized(loc) {
            val posL = DriveSubsystem.leftPosition
            val posR = DriveSubsystem.rightPosition

            val angA = AHRS.correctedAngle

            val deltaL = posL - prevL
            val deltaR = posR - prevR
            val deltaA = angA - prevA

            val kinematics = Kinematics.forwardKinematics(deltaL.FT, deltaR.FT, deltaA.radians)
            robotPosition = robotPosition.transformBy(Pose2d.fromTwist(kinematics))

            prevL = posL
            prevR = posR
            prevA = angA
        }
    }
}

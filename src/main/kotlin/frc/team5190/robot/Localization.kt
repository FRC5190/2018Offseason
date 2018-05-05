package frc.team5190.robot

import edu.wpi.first.wpilibj.Notifier
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.sensors.Pigeon
import frc.team5190.robot.util.Maths
import jaci.pathfinder.Pathfinder
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

object Localization {
    private val loopSync = Object()

    var robotPosition = Vector2D.ZERO
        private set

    private var leftEncoderLastPosition = 0
    private var rightEncoderLastPosition = 0
    private var gyroLastAngle = 0.0

    init {
        reset()
        Notifier(this::run).startPeriodic(0.05)
    }

    fun reset(startingPosition: Vector2D = Vector2D.ZERO) {
        synchronized(loopSync) {
            robotPosition = startingPosition
            leftEncoderLastPosition = DriveSubsystem.leftEncoderPosition
            rightEncoderLastPosition = DriveSubsystem.rightEncoderPosition
            gyroLastAngle = Pigeon.correctedAngle
        }
    }
    private fun run() {
        synchronized(loopSync) {
            val leftEncoderPosition = DriveSubsystem.leftEncoderPosition
            val rightEncoderPosition = DriveSubsystem.rightEncoderPosition
            val gyroAngle = Pigeon.correctedAngle

            val leftEncoderDistanceDelta = Maths.nativeUnitsToFeet(leftEncoderPosition - leftEncoderLastPosition)
            val rightEncoderDistanceDelta = Maths.nativeUnitsToFeet(rightEncoderPosition - rightEncoderLastPosition)
            val gyroAngleDelta = Math.toRadians(Pathfinder.boundHalfDegrees(gyroAngle - gyroLastAngle))

            val distanceTraveled = (leftEncoderDistanceDelta + rightEncoderDistanceDelta) / 2.0

            val sinTheta = Math.sin(gyroAngleDelta)
            val cosTheta = Math.cos(gyroAngleDelta)
            val s: Double
            val c: Double
            if (Math.abs(gyroAngleDelta) < 1E-9) {
                s = 1.0 - 1.0 / 6.0 * gyroAngleDelta * gyroAngleDelta
                c = .5 * gyroAngleDelta
            } else {
                s = sinTheta / gyroAngleDelta
                c = (1.0 - cosTheta) / gyroAngleDelta
            }
            robotPosition.add(Vector2D(distanceTraveled * s, distanceTraveled * c))

            leftEncoderLastPosition = leftEncoderPosition
            rightEncoderLastPosition = rightEncoderPosition
            gyroLastAngle = gyroAngle
        }
    }
}
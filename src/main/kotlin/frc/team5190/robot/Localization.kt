package frc.team5190.robot

import edu.wpi.first.wpilibj.Notifier
import frc.team5190.robot.drive.Drive
import frc.team5190.robot.sensors.Pigeon
import frc.team5190.lib.util.Maths
import jaci.pathfinder.Pathfinder
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

object Localization {

    private val synchronousOdometry = Object()

    var robotPosition: Vector2D = Vector2D.ZERO
        private set

    private var leftLastPos = 0
    private var rightLastPos = 0
    private var gyroLastAngle = 0.0

    init {
        reset()
        Notifier(this::run).startPeriodic(0.05)
    }

    fun reset(startingPosition: Vector2D = Vector2D.ZERO) {
        synchronized(synchronousOdometry) {
            robotPosition = startingPosition
            leftLastPos = Drive.leftPosition
            rightLastPos = Drive.rightPosition
            gyroLastAngle = Pigeon.correctedAngle
        }
    }

    private fun run() {
        synchronized(synchronousOdometry) {

            val leftPos = Drive.leftPosition
            val rightPos = Drive.rightPosition
            val gyroAngle = Pigeon.correctedAngle

            val dleft = Maths.nativeUnitsToFeet(leftPos - leftLastPos)
            val dright = Maths.nativeUnitsToFeet(rightPos - rightLastPos)
            val dgyroangle = Math.toRadians(Pathfinder.boundHalfDegrees(gyroAngle - gyroLastAngle))

            val distanceTraveled = (dleft + dright) / 2.0

            val sinTheta = Math.sin(dgyroangle)
            val cosTheta = Math.cos(dgyroangle)

            val s: Double
            val c: Double

            if (Math.abs(dgyroangle) < 1E-9) {
                s = 1.0 - 1.0 / 6.0 * dgyroangle * dgyroangle
                c = .5 * dgyroangle
            } else {
                s = sinTheta / dgyroangle
                c = (1.0 - cosTheta) / dgyroangle
            }

            robotPosition.add(Vector2D(distanceTraveled * s, distanceTraveled * c))

            leftLastPos = leftPos
            rightLastPos = rightPos
            gyroLastAngle = gyroAngle
        }
    }
}
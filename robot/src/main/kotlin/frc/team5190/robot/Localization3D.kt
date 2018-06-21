@file:Suppress("MemberVisibilityCanBePrivate")

package frc.team5190.robot

import edu.wpi.first.wpilibj.Notifier
import frc.team5190.lib.cos
import frc.team5190.lib.epsilonEquals
import frc.team5190.lib.sin
import frc.team5190.lib.units.Distance
import frc.team5190.lib.units.NativeUnits
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.sensors.NavX
import jaci.pathfinder.Pathfinder
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import kotlin.math.cos
import kotlin.math.sin

object Localization3D {

    private val loc = Object()
    private val zeroPitch = NavX.pitch.toDouble()

    var robotPosition = Vector3D.ZERO
        private set

    private var leftLastPos: Distance = NativeUnits(0)
    private var rightLastPos: Distance = NativeUnits(0)
    private var gyroLastAngle = 0.0

    init {
        reset()
        Notifier(this::run).startPeriodic(0.05)
    }

    fun reset(position: Vector3D = Vector3D.ZERO) {
        synchronized(loc) {
            robotPosition = position
            leftLastPos = DriveSubsystem.leftPosition
            rightLastPos = DriveSubsystem.rightPosition
            gyroLastAngle = NavX.correctedAngle
        }
    }

    private fun run() {
        synchronized(loc) {
            val leftPos = DriveSubsystem.leftPosition
            val rightPos = DriveSubsystem.rightPosition
            val gyroAngle = NavX.correctedAngle
            val gyropitch = NavX.pitch - zeroPitch

            val dleft = leftPos - leftLastPos
            val dright = rightPos - rightLastPos
            val dgyroangle = Math.toRadians(Pathfinder.boundHalfDegrees(gyroAngle - gyroLastAngle))

            val d = (dleft + dright).FT.value / 2.0

            val sinTheta = sin(dgyroangle)
            val cosTheta = cos(dgyroangle)

            val s: Double
            val c: Double

            if (dgyroangle epsilonEquals 0.0) {
                s = 1.0 - 1.0 / 6.0 * dgyroangle * dgyroangle
                c = .5 * dgyroangle
            } else {
                s = sinTheta / dgyroangle
                c = (1.0 - cosTheta) / dgyroangle
            }

            val x = d * s
            val y = d * c

            val lastAngleRad = Math.toRadians(gyroLastAngle)
            val lastAngleCos = cos(lastAngleRad)
            val lastAngleSin = sin(lastAngleRad)

            val twodimvector = Vector2D(
                    x * lastAngleCos - y * lastAngleSin,
                    x * lastAngleSin + y * lastAngleCos)

            robotPosition = robotPosition.add(Vector3D(
                    twodimvector.x cos gyropitch,
                    twodimvector.y cos gyropitch,
                    d sin gyropitch
            ))

            leftLastPos = leftPos
            rightLastPos = rightPos
            gyroLastAngle = gyroAngle
        }
    }
}

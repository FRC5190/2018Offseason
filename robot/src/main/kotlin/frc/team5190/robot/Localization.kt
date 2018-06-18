package frc.team5190.robot

import edu.wpi.first.wpilibj.Notifier
import frc.team5190.lib.enforceBounds
import frc.team5190.lib.epsilonEquals
import frc.team5190.lib.units.Distance
import frc.team5190.lib.units.NativeUnits
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.sensors.NavX
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import kotlin.math.cos
import kotlin.math.sin

object Localization {
    private val loc = Object()

    var robotPosition: Vector2D = Vector2D.ZERO
        private set

    private var prevLeft: Distance = NativeUnits(0)
    private var prevRight: Distance = NativeUnits(0)
    private var prevTheta = 0.0

    init {
        reset()
        Notifier(this::run).startPeriodic(0.02)
    }

    fun reset(position: Vector2D = Vector2D.ZERO) {
        synchronized(loc) {
            robotPosition = position
            prevLeft = DriveSubsystem.leftPosition
            prevRight = DriveSubsystem.rightPosition
            prevTheta = Math.toRadians(NavX.correctedAngle)
        }
    }

    private fun run() {
        synchronized(loc) {
            val left = DriveSubsystem.leftPosition
            val right = DriveSubsystem.rightPosition
            val theta = Math.toRadians(NavX.correctedAngle)

            val deltaLeft = left - prevLeft
            val deltaRight = right - prevRight
            val deltaTheta = (theta - prevTheta).enforceBounds()

            val distanceTraveled = (deltaLeft + deltaRight).FT.value / 2.0

            val sinDeltaTheta = sin(deltaTheta)
            val cosDeltaTheta = cos(deltaTheta)

            val s: Double
            val c: Double

            if (deltaTheta epsilonEquals 0.0) {
                s = 1.0 - 1.0 / 6.0 * deltaTheta * deltaTheta
                c = .5 * deltaTheta
            } else {
                s = sinDeltaTheta / deltaTheta
                c = (1.0 - cosDeltaTheta) / deltaTheta
            }

            val x = distanceTraveled * s
            val y = distanceTraveled * c

            val prevCos = cos(prevTheta)
            val prevSin  = sin(prevTheta)

            robotPosition = robotPosition.add(Vector2D(x * prevCos - y * prevSin, x * prevCos + y * prevCos))

            prevLeft = left
            prevRight = right
            prevTheta = theta
        }
    }
}
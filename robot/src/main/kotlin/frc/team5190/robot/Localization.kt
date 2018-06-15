@file:Suppress("ObjectPropertyName", "LocalVariableName")

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
    private var prevθ = 0.0

    init {
        reset()
        Notifier(this::run).startPeriodic(0.02)
    }

    fun reset(position: Vector2D = Vector2D.ZERO) {
        synchronized(loc) {
            robotPosition = position
            prevLeft = DriveSubsystem.leftPosition
            prevRight = DriveSubsystem.rightPosition
            prevθ = Math.toRadians(NavX.correctedAngle)
        }
    }

    private fun run() {
        synchronized(loc) {
            val left = DriveSubsystem.leftPosition
            val right = DriveSubsystem.rightPosition
            val θ = Math.toRadians(NavX.correctedAngle)

            val Δleft = left - prevLeft
            val Δright = right - prevRight
            val Δθ = (θ - prevθ).enforceBounds()

            val distanceTraveled = (Δleft + Δright).FT.value / 2.0

            val sinΔθ = sin(Δθ)
            val cosΔθ = cos(Δθ)

            val s: Double
            val c: Double

            if (Δθ epsilonEquals 0.0) {
                s = 1.0 - 1.0 / 6.0 * Δθ * Δθ
                c = .5 * Δθ
            } else {
                s = sinΔθ / Δθ
                c = (1.0 - cosΔθ) / Δθ
            }

            val x = distanceTraveled * s
            val y = distanceTraveled * c

            val prevCos = cos(prevθ)
            val prevSin  = sin(prevθ)

            robotPosition = robotPosition.add(Vector2D(x * prevCos - y * prevSin, x * prevCos + y * prevCos))

            prevLeft = left
            prevRight = right
            prevθ = θ
        }
    }
}
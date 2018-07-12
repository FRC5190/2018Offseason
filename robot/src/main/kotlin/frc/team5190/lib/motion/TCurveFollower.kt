/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.motion

import frc.team5190.lib.control.PositionPIDFController
import kotlin.math.sqrt

class TCurveFollower(private val initialPos: Double,
                     targetPos: Double,
                     private var cruiseVelocity: Double,
                     private val acceleration: Double) {

    // Variables on initialization
    private var tstart = 0.0
    private var hasStartedProfile = false

    // Track of time elapsed
    var t = 0.0
        private set

    // Total path distance
    private val totalDistance = targetPos - initialPos

    // Time spent accelerating
    private val taccel = (cruiseVelocity / acceleration).let { trapezoidalTime ->
        if (0.5 * acceleration * trapezoidalTime * trapezoidalTime > totalDistance / 2.0) {
            val triangularTime = sqrt(totalDistance / acceleration)
            cruiseVelocity = acceleration * triangularTime
            return@let triangularTime
        } else {
            return@let trapezoidalTime
        }
    }

    // Distance accelerating
    private val daccel = 0.5 * acceleration * taccel * taccel

    // Time Cruising
    private val tcruise = ((totalDistance - (2 * daccel)) / cruiseVelocity).coerceAtLeast(0.0)

    // Distance cruising
    private val dcruise = tcruise * cruiseVelocity

    // PIDF Controller
    private var pidfController = PositionPIDFController(current = {0.0})

    // Total time for path
    val tpath = taccel + tcruise + taccel


    // Set PIDF Values for PID Controller
    fun setPIDFValues(proportional: Double,
                      integral: Double,
                      derivative: Double,
                      velocityFF: Double,
                      vi: Double,
                      integralZone: Double,
                      currentPosition: () -> Double) {

        pidfController = PositionPIDFController(
                kP = proportional,
                kI = integral,
                kD = derivative,
                kV = velocityFF,
                kS = vi,
                kILimit = integralZone,
                current = currentPosition
        )
    }

    // Get output to apply to motors
    fun getOutput(): Triple<Double, Double, Double> {
        // Initialize variables
        if (!hasStartedProfile) {
            initialize()
        } else {
            t = ((System.currentTimeMillis() / 1000.0) - tstart)
        }

        val velocity: Double
        val position: Double

        when {
        // Acceleration Phase
            t < taccel -> {
                val t1 = t

                velocity = velocityEquation(v0 = 0.0, a = acceleration, t = t1)
                position = positionEquation(s0 = 0.0, v0 = 0.0, a = acceleration, t = t1)
            }
        // Cruising Phase
            t < taccel + tcruise -> {
                val t2 = t - taccel

                velocity = velocityEquation(v0 = cruiseVelocity, a = 0.0, t = t2)
                position = positionEquation(s0 = daccel, v0 = cruiseVelocity, a = 0.0, t = t2)
            }
        // Deceleration Phase
            t < taccel + tcruise + taccel -> {
                val t3 = t - taccel - tcruise

                velocity = velocityEquation(v0 = cruiseVelocity, a = -acceleration, t = t3)
                position = positionEquation(s0 = daccel + dcruise, v0 = cruiseVelocity, a = -acceleration, t = t3)
            }
        // Rest
            else -> {
                velocity = 0.0
                position = 0.0
            }
        }
        return Triple(pidfController.getPIDFOutput(Triple(position, velocity, acceleration)), position, velocity)
    }

    private fun positionEquation(s0: Double, v0: Double, a: Double, t: Double) = s0 + (v0 * t) + (0.5 * a * t * t)
    private fun velocityEquation(v0: Double, a: Double, t: Double) = v0 + (a * t)


    private fun initialize() {
        hasStartedProfile = true
        tstart = System.currentTimeMillis() / 1000.0
        t = 0.0
    }
}
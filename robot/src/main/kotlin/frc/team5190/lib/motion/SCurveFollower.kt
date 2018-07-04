package frc.team5190.lib.motion

import frc.team5190.lib.control.PositionPIDFController
import kotlin.math.sqrt

class SCurveFollower(private val initialPos: Double,
                     targetPos: Double,
                     private var cruiseVelocity: Double,
                     averageAcceleration: Double,
                     private val jerk: Double) {

    // Variables on initialization
    private var tstart = 0.0
    private var hasStartedProfile = false

    // Track of time elapsed
    var t = 0.0
        private set

    // Total path distance
    private val totalDistance = targetPos - initialPos

    private val maxAcceleration = sqrt(cruiseVelocity * jerk)

    // Time spent accelerating
    private val taccel = cruiseVelocity / averageAcceleration

    private val tconcave = taccel / 2.0
    private val tconvex = taccel / 2.0

    private val dconcave = positionEquation(s0 = 0.0, v0 = 0.0, a0 = 0.0, j = jerk, t = tconcave)
    private val dconvex = positionEquation(s0 = 0.0, v0 = cruiseVelocity / 2.0, a0 = maxAcceleration, j = -jerk, t = tconvex)

    private val tcruise = (totalDistance - (2 * dconcave) - (2 * dconvex)) / cruiseVelocity
    private val dcruise = (cruiseVelocity * tcruise)


    // PIDF Controller
    private val pidfController = PositionPIDFController()

    // Total time for path
    val tpath = tconcave + tconvex + tcruise + tconvex + tconcave


    // Set PIDF Values for PID Controller
    fun setPIDFValues(proportional: Double,
                      integral: Double,
                      derivative: Double,
                      velocityFF: Double,
                      vi: Double,
                      integralZone: Double) {
        pidfController.apply {
            p = proportional
            i = integral
            d = derivative
            v = velocityFF
            vIntercept = vi
            izone = integralZone
        }
    }

    fun getOutput(currentPos: Double): Triple<Double, Double, Double> {
        // Initialize variables
        if (!hasStartedProfile) {
            initialize()
        } else {
            t = ((System.currentTimeMillis() / 1000.0) - tstart)
        }

        val velocity: Double
        val position: Double

        when {
        // Acceleration Concave Phase
            t < tconcave -> {
                val t1 = t

                velocity = velocityEquation(v0 = 0.0, a0 = 0.0, j = jerk, t = t1)
                position = positionEquation(s0 = 0.0, v0 = 0.0, a0 = 0.0, j = jerk, t = t1)
            }
        // Acceleration Convex Phase
            t < tconcave + tconvex -> {
                val t2 = t - tconcave
                val vh = cruiseVelocity / 2.0

                velocity = velocityEquation(v0 = vh, a0 = maxAcceleration, j = -jerk, t = t2)
                position = positionEquation(s0 = dconcave, v0 = vh, a0 = maxAcceleration, j = -jerk, t = t2)
            }
        // Cruising
            t < tconcave + tconvex + tcruise -> {
                val t3 = t - tconcave - tconvex

                velocity = velocityEquation(v0 = cruiseVelocity, a0 = 0.0, j = 0.0, t = t3)
                position = positionEquation(s0 = dconcave + dconvex, v0 = cruiseVelocity, a0 = 0.0, j = 0.0, t = t3)
            }
        // Deceleration Convex Phase
            t < tconcave + tconvex + tcruise + tconvex -> {
                val t4 = t - tconcave - tconvex - tcruise

                velocity = velocityEquation(v0 = cruiseVelocity, a0 = 0.0, j = -jerk, t = t4)
                position = positionEquation(s0 = dconcave + dconvex + dcruise, v0 = cruiseVelocity, a0 = 0.0, j = -jerk, t = t4)
            }
        // Deceleration Concave Phase
            t < tconcave + tconvex + tcruise + tconvex + tconcave -> {
                val t5 = t - tconcave - tconvex - tcruise - tconvex
                val vh = cruiseVelocity / 2.0

                velocity = velocityEquation(v0 = vh, a0 = -maxAcceleration, j = jerk, t = t5)
                position = positionEquation(s0 = dconcave + dconvex + dcruise + dconvex, v0 = vh, a0 = -maxAcceleration, j = jerk, t = t5)
            }
        // Rest
            else -> {
                velocity = 0.0
                position = 0.0
            }
        }
        return Triple(pidfController.getPIDFOutput(position + initialPos, velocity, currentPos), velocity, position)
    }


    private fun positionEquation(s0: Double, v0: Double, a0: Double, j: Double, t: Double) = s0 + (v0 * t) + (0.5 * a0 * t * t) + (0.167 * j * t * t * t)
    private fun velocityEquation(v0: Double, a0: Double, j: Double, t: Double) = v0 + (a0 * t) + (0.5 * j * t * t)

    private fun initialize() {
        hasStartedProfile = true
        tstart = System.currentTimeMillis() / 1000.0
        t = 0.0
    }

}
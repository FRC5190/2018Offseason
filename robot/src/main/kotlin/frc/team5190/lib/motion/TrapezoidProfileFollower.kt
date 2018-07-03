package frc.team5190.lib.motion

import frc.team5190.lib.control.PositionPIDFController
import kotlin.math.sqrt

class TrapezoidProfileFollower(private val initialPos: Double,
                               targetPos: Double,
                               private val acceleration: Double,
                               private var cruiseVelocity: Double) {

    private var tstart = 0.0
    private var hasStartedProfile = false

    var t = 0.0
        private set

    private val totalDistance = targetPos - initialPos
    private val taccel = (cruiseVelocity / acceleration).let {
        if (0.5 * acceleration * it * it > totalDistance / 2) {
            cruiseVelocity = acceleration * sqrt(totalDistance / acceleration)
            sqrt(totalDistance / acceleration)
        } else it
    }

    private val daccel = 0.5 * acceleration * taccel * taccel
    private val tcruise = ((totalDistance - (2 * daccel)) / cruiseVelocity).coerceAtLeast(0.0)

    private val pidfController = PositionPIDFController()

    val tpath = taccel + tcruise + taccel

    fun getOutput(currentPos: Double): Pair<Double, Double> {
        if (!hasStartedProfile) {
            hasStartedProfile = true
            tstart = System.currentTimeMillis() / 1000.0
        } else {
            t = ((System.currentTimeMillis() / 1000.0) - tstart)
        }

        when {
            t < taccel -> {
                val velocity = acceleration * t
                val position = 0.5 * acceleration * t * t

                return pidfController.getPIDFOutput(position + initialPos, velocity, currentPos) to velocity
            }
            t < taccel + tcruise -> {
                val velocity = cruiseVelocity
                val position = cruiseVelocity * (t - taccel)

                return pidfController.getPIDFOutput(position + initialPos, velocity, currentPos) to velocity
            }
            t < taccel + tcruise + taccel -> {
                val velocity = cruiseVelocity - (acceleration * (t - tcruise - taccel))
                val position = 0.5 * acceleration * (t - tcruise - taccel) * (t - tcruise - taccel)

                return pidfController.getPIDFOutput(position + initialPos, velocity, currentPos) to velocity
            }
            else -> return 0.0 to 0.0
        }
    }
}
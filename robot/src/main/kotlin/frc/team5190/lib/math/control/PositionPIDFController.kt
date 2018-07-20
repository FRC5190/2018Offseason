/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.math.control

import frc.team5190.lib.extensions.epsilonEquals
import kotlin.math.sign

class PositionPIDFController(private val kP: Double = 0.0,
                             private val kI: Double = 0.0,
                             private val kD: Double = 0.0,
                             private val kV: Double = 0.0,
                             private val kA: Double = 0.0,
                             private val kS: Double = 0.0,
                             private val kDt: Double = 0.02,
                             private val kILimit: Double = 0.0,
                             private val current: () -> Double

) {

    private var lastError = 0.0
    private var derivative = 0.0
    private var integral = 0.0


    fun getPIDFOutput(target: Triple<Double, Double, Double>): Double {
        val (targetPosition, targetVelocity, targetAcceleration) = target
        val current = current()

        val error = targetPosition - current

        integral += error * kDt
        derivative = (error - lastError) / kDt

        if (integral > kILimit && (kILimit epsilonEquals 0.0).not()) integral = kILimit

        val feedback = (kP * error) + (kI * integral) + (kD * derivative)
        val feedfrwd = (kV * targetVelocity) + (kA * targetAcceleration) + (kS * sign(targetPosition))

        lastError = error

        return feedback + feedfrwd
    }
}

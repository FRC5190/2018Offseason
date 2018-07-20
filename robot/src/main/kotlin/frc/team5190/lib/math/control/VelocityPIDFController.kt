/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.math.control

import frc.team5190.lib.extensions.epsilonEquals
import kotlin.math.absoluteValue
import kotlin.math.sign

class VelocityPIDFController(private val kP: Double = 0.0,
                             private val kI: Double = 0.0,
                             private val kD: Double = 0.0,
                             private val kV: Double = 0.0,
                             private val kA: Double = 0.0,
                             private val kS: Double = 0.0,
                             private val kDt: Double = 0.02,
                             private val kILimit: Double = 0.0,
                             private val kDeadband: Double = 0.1,
                             private val current: () -> Double) {


    private var lastError = 0.0
    private var derivative = 0.0
    private var integral = 0.0

    fun getPIDFOutput(target: Pair<Double, Double>): Double {
        val (targetVelocity, targetAcceleration) = target
        val current = current()

        val error = targetVelocity - current

        integral += error * kDt
        derivative += (error - lastError) / kDt

        if (integral > kILimit && (kILimit epsilonEquals 0.0).not()) integral = kILimit

        if (targetVelocity.absoluteValue < kDeadband) return 0.0

        val feedback = (kP * error) + (kI * integral) + (kD * derivative)
        val feedfrwd = (kV * targetVelocity) + (kA * targetAcceleration) + (kS * sign(targetVelocity))

        lastError = error

        return feedback + feedfrwd
    }
}

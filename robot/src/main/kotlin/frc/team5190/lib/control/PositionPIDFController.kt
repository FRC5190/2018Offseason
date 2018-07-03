package frc.team5190.lib.control

import kotlin.math.absoluteValue
import kotlin.math.sign

class PositionPIDFController {
    var p = 0.0
    var i = 0.0
    var izone = 0.0
    var d = 0.0
    var v = 0.0
    var vIntercept = 0.0

    var deadband = 0.01

    var dt = 0.02

    private var lastError = 0.0
    private var derivative = 0.0
    private var integral = 0.0

    fun getPIDFOutput(targetPos: Double, targetVelocity: Double, actualPos: Double): Double {
        val error = targetPos - actualPos

        integral += (error * dt)
        derivative = (error - lastError) / dt

        if (izone > 0.0 && integral > izone) integral = 0.0

        val output = if (targetPos.absoluteValue > deadband) {
            (p * error) + (i * integral) + (d * derivative) + (v * targetVelocity) + (vIntercept * sign(targetVelocity))
        } else {
            0.0
        }

        lastError = error

        return output
    }
}

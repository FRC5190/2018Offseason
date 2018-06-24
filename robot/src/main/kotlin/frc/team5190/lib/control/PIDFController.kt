package frc.team5190.lib.control

import kotlin.math.absoluteValue
import kotlin.math.sign

class PIDFController {
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

    fun getPIDFOutput(target: Double, actual: Double): Double {
        val error = target - actual
        
        integral += (error * dt)
        derivative = (error - lastError) / dt

        if (izone > 0.0 && integral > izone) integral = 0.0

        val output = if (target.absoluteValue > deadband) {
            (p * error) + (i * integral) + (d * derivative) + (v * target) + (vIntercept * sign(target))
        } else {
            0.0
        }
  
        lastError = error

        return output
    }
}

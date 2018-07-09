package frc.team5190.lib.pid

import kotlin.math.absoluteValue
import kotlin.math.sign

class VelocityPIDFController {
    var kP = 0.0
    var kI = 0.0
    var izone = 0.0
    var kD = 0.0
    var kV = 0.0
    var kVIntercept = 0.0

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
            (kP * error) + (kI * integral) + (kD * derivative) + (kV * target) + (kVIntercept * sign(target))
        } else {
            0.0
        }
  
        lastError = error

        return output
    }
}

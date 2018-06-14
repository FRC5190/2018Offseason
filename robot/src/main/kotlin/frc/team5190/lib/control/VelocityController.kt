package frc.team5190.lib.control

import frc.team5190.lib.units.Speed

// Simple Velocity PV controller for following a certain velocity
class VelocityController {

    // P, V, V intercept variables
    var p = 0.0
    var v = 0.0
    var vIntercept = 0.0

    // Returns output
    fun calculateOutput(targetSpeed: Speed, actualVelocity: Speed): Double {
        val velocityError = targetSpeed - actualVelocity

        val feedForward = v * targetSpeed.FPS.value + vIntercept
        val feedback = p * velocityError.FPS.value

        return (feedForward + feedback).coerceIn(-0.4, 0.6)
    }
}
package frc.team5190.lib.motion

class SCurveFollower(private val initialPos: Double,
                     targetPos: Double,
                     cruiseVelocity: Double,
                     private val maxAcceleration: Double,
                     private val jerk: Double) {

    /*
    // Variables on initialization
    private var tstart = 0.0
    private var hasStartedProfile = false

    // Track of time elapsed
    var t = 0.0
        private set

    // Total path distance
    private val distance = targetPos - initialPos

    // Cruise Velocity
    private val vmax: Double

    init {
        val a2 = maxAcceleration * maxAcceleration
        val j2 = jerk * jerk

        vmax = min(cruiseVelocity,
                (-a2 + sqrt(a2 * a2 + 4 * (j2 * maxAcceleration * distance))) / (2 * jerk))
    }

   
    private val tconcave = maxAcceleration / jerk
    private val dconcave = positionEquation(s0 = 0.0, v0 = 0.0, a0 = 0.0, j = jerk, t = tconcave)
    private val vaconcave = velocityEquation(v0 = 0.0, a0 = 0.0, j = jerk, t = tconcave)

    private val tlinear = (cruiseVelocity - vaconcave - vaconcave) / maxAcceleration
    private val dlinear = positionEquation(s0 = 0.0, v0 = vaconcave, a0 = maxAcceleration, j = 0.0, t = tlinear)
    private val valinear = velocityEquation(v0 = vaconcave, a0 = maxAcceleration, j = 0.0, t = tlinear)
   
    private val tconvex = maxAcceleration / jerk
    private val dconvex = positionEquation(s0 = 0.0, v0 = valinear, a0 = maxAcceleration, j = -jerk, t = tconvex)
    private val vaconvex = velocityEquation(v0 = valinear, a0 = maxAcceleration, j = -jerk, t = tconvex)
    private val vdconvex = velocityEquation(v0 = vaconvex, a0 = 0.0, j = -jerk, t = tconvex)

    private val vdlinear = velocityEquation(v0 = vdconvex, a0 = -maxAcceleration, j = 0.0, t = tlinear)

    // Time and distance at end of cruise
    private val tcruise = (distance - (2 * dconcave) - (2 * dconvex)) / vmax
    private val dcruise = (vmax * tcruise)

    // PIDF Controller
    private val pidfController = PositionPIDFController()

    // Total time for path
    val tpath = tconcave + tlinear + tconvex + tcruise + tconvex +tlinear + tconcave


    // Set PIDF Values for PID Controller
    fun setPIDFValues(proportional: Double, integral: Double, derivative: Double, velocityFF: Double, vi: Double, integralZone: Double) {
        pidfController.apply {
            kP = proportional
            kI = integral
            kD = derivative
            kV = velocityFF
            kVIntercept = vi
            kIZone = integralZone
        }
    }

    // Returns output (PID output, target velocity, target position)
    fun getOutput(currentPos: Double): Triple<Double, Double, Double> {
        // Initialize variables
        if (!hasStartedProfile) {
            initialize()
        } else {
            t = ((System.currentTimeMillis() / 1000.0) - tstart)
        }

        val velocity: Double
        val position: Double
        val acceleration: Double

        when {
        // Acceleration Concave Phase
            t < tconcave -> {
                val t1 = t

                velocity = velocityEquation(v0 = 0.0, a0 = 0.0, j = jerk, t = t1)
                position = positionEquation(s0 = 0.0, v0 = 0.0, a0 = 0.0, j = jerk, t = t1)
                acceleration = accelerationEquation(a0 = 0.0, j = jerk, t = t1)
            }
        // Acceleration Linear Phase
            t < tconcave + tlinear -> {
                val t2 = t - tconcave

                velocity = velocityEquation(v0 = vaconcave, a0 = maxAcceleration, j = 0.0, t = t2)
                position = positionEquation(s0 = dconcave, v0 = vaconcave, a0 = maxAcceleration, j = 0.0, t = t2)
                acceleration = accelerationEquation(a0 = maxAcceleration, j = 0.0, t = t2)
            }
        // Acceleration Convex Phase
            t < tconcave + tlinear + tconvex -> {
                val t3 = t - tconcave - tlinear

                velocity = velocityEquation(v0 = valinear, a0 = maxAcceleration, j = -jerk, t = t3)
                position = positionEquation(s0 = dconcave + dlinear, v0 = valinear, a0 = maxAcceleration, j = -jerk, t = t3)
                acceleration = accelerationEquation(a0 = maxAcceleration, j = -jerk, t = t3)
            }
        // Cruising
            t < tconcave + tlinear + tconvex + tcruise -> {
                val t4 = t - tconcave - tlinear - tconvex

                velocity = velocityEquation(v0 = vmax, a0 = 0.0, j = 0.0, t = t4)
                position = positionEquation(s0 = dconcave + dlinear + dconvex, v0 = vmax, a0 = 0.0, j = 0.0, t = t4)
                acceleration = accelerationEquation(a0 = 0.0, j = 0.0, t = t4)
            }
        // Deceleration Convex Phase
            t < tconcave + tlinear + tconvex + tcruise + tconvex -> {
                val t5 = t - tconcave - tlinear - tconvex - tcruise

                velocity = velocityEquation(v0 = vmax, a0 = 0.0, j = -jerk, t = t5)
                position = positionEquation(s0 = dconcave + dlinear + dconvex + dcruise, v0 = vmax, a0 = 0.0, j = -jerk, t = t5)
                acceleration = accelerationEquation(a0 = 0.0, j = -jerk, t = t5)
            }
        // Deceleration Linear Phase
            t < tconcave + tlinear + tconvex + tcruise + tconvex + tlinear -> {
                val t6 = t - tconcave - tlinear - tconvex - tcruise - tconcave

                velocity = velocityEquation(v0 = vdconvex, a0 = -maxAcceleration, j = 0.0, t = t6)
                position = positionEquation(s0 = dconcave + dlinear + dconvex + dcruise + dconvex, v0 = vdconvex, a0 = -maxAcceleration, j = 0.0, t = t6)
                acceleration = accelerationEquation(a0 = -maxAcceleration, j = 0.0, t = t6)
            }
        // Deceleration Concave Phase
            t < tconcave + tlinear + tconvex + tcruise + tconvex + +tlinear + tconcave -> {
                val t7 = t - tconcave - tlinear - tconvex - tcruise - tconvex

                velocity = velocityEquation(v0 = vdlinear, a0 = -maxAcceleration, j = jerk, t = t7)
                position = positionEquation(s0 = dconcave + dlinear + dconvex + dcruise + dconvex + dlinear, v0 = vdlinear, a0 = -maxAcceleration, j = jerk, t = t7)
                acceleration = accelerationEquation(a0 = -maxAcceleration, j = jerk, t = t7)
            }
        // Rest
            else -> {
                velocity = 0.0
                position = 0.0
                acceleration = 0.0
            }
        }
        return Triple(pidfController.getPIDFOutput(position + initialPos, velocity, currentPos), velocity, acceleration)
    }

    fun getTestOutput(t: Double): Triple<Double, Double, Double> {
        val velocity: Double
        val position: Double
        val acceleration: Double

        when {
        // Acceleration Concave Phase
            t < tconcave -> {
                val t1 = t

                velocity = velocityEquation(v0 = 0.0, a0 = 0.0, j = jerk, t = t1)
                position = positionEquation(s0 = 0.0, v0 = 0.0, a0 = 0.0, j = jerk, t = t1)
                acceleration = accelerationEquation(a0 = 0.0, j = jerk, t = t1)
            }
        // Acceleration Linear Phase
            t < tconcave + tlinear -> {
                val t2 = t - tconcave

                velocity = velocityEquation(v0 = vaconcave, a0 = maxAcceleration, j = 0.0, t = t2)
                position = positionEquation(s0 = dconcave, v0 = vaconcave, a0 = maxAcceleration, j = 0.0, t = t2)
                acceleration = accelerationEquation(a0 = maxAcceleration, j = 0.0, t = t2)
            }
        // Acceleration Convex Phase
            t < tconcave + tlinear + tconvex -> {
                val t3 = t - tconcave - tlinear

                velocity = velocityEquation(v0 = valinear, a0 = maxAcceleration, j = -jerk, t = t3)
                position = positionEquation(s0 = dconcave + dlinear, v0 = valinear, a0 = maxAcceleration, j = -jerk, t = t3)
                acceleration = accelerationEquation(a0 = maxAcceleration, j = -jerk, t = t3)
            }
        // Cruising
            t < tconcave + tlinear + tconvex + tcruise -> {
                val t4 = t - tconcave - tlinear - tconvex

                velocity = velocityEquation(v0 = vmax, a0 = 0.0, j = 0.0, t = t4)
                position = positionEquation(s0 = dconcave + dlinear + dconvex, v0 = vmax, a0 = 0.0, j = 0.0, t = t4)
                acceleration = accelerationEquation(a0 = 0.0, j = 0.0, t = t4)
            }
        // Deceleration Convex Phase
            t < tconcave + tlinear + tconvex + tcruise + tconvex -> {
                val t5 = t - tconcave - tlinear - tconvex - tcruise

                velocity = velocityEquation(v0 = vmax, a0 = 0.0, j = -jerk, t = t5)
                position = positionEquation(s0 = dconcave + dlinear + dconvex + dcruise, v0 = vmax, a0 = 0.0, j = -jerk, t = t5)
                acceleration = accelerationEquation(a0 = 0.0, j = -jerk, t = t5)
            }
        // Deceleration Linear Phase
            t < tconcave + tlinear + tconvex + tcruise + tconvex + tlinear -> {
                val t6 = t - tconcave - tlinear - tconvex - tcruise - tconvex

                velocity = velocityEquation(v0 = vdconvex, a0 = -maxAcceleration, j = 0.0, t = t6)
                position = positionEquation(s0 = dconcave + dlinear + dconvex + dcruise + dconvex, v0 = vdconvex, a0 = -maxAcceleration, j = 0.0, t = t6)
                acceleration = accelerationEquation(a0 = -maxAcceleration, j = 0.0, t = t6)
            }
        // Deceleration Concave Phase
            t < tconcave + tlinear + tconvex + tcruise + tconvex +tlinear + tconcave -> {
                val t7 = t - tconcave - tlinear - tconvex - tcruise - tconvex - tlinear

                velocity = velocityEquation(v0 = vdlinear, a0 = -maxAcceleration, j = jerk, t = t7)
                position = positionEquation(s0 = dconcave + dlinear + dconvex + dcruise + dconvex + dlinear, v0 = vdlinear, a0 = -maxAcceleration, j = jerk, t = t7)
                acceleration = accelerationEquation(a0 = -maxAcceleration, j = jerk, t = t7)
            }
        // Rest
            else -> {
                velocity = 0.0
                position = 0.0
                acceleration = 0.0
            }
        }
        return Triple(position, velocity, acceleration)
    }


    private fun positionEquation(s0: Double, v0: Double, a0: Double, j: Double, t: Double) = s0 + (v0 * t) + (0.5 * a0 * t * t) + (0.167 * j * t * t * t)
    private fun velocityEquation(v0: Double, a0: Double, j: Double, t: Double) = v0 + (a0 * t) + (0.5 * j * t * t)
    private fun accelerationEquation(a0: Double, j: Double, t: Double) = a0 + (j * t)

    private fun initialize() {
        hasStartedProfile = true
        tstart = System.currentTimeMillis() / 1000.0
        t = 0.0
    }
    */
}
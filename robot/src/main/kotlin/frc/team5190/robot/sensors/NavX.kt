package frc.team5190.robot.sensors

import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.SPI
import jaci.pathfinder.Pathfinder

/**
 * Creates a NavX singleton object
 */
object NavX : AHRS(SPI.Port.kMXP) {

    init {
        reset()

    }

    var angleOffset = 0.0
    var pitchOffset = 0.0

    val correctedAngle: Double
        get() = Pathfinder.boundHalfDegrees(-angle + angleOffset)

    val correctedPitch: Double
        get() = Pathfinder.boundHalfDegrees(roll.toDouble() - pitchOffset)
}

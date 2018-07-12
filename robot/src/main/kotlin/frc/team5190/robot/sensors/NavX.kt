/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.sensors

import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.SPI
import frc.team5190.lib.extensions.boundDegrees

/**
 * Creates a NavX singleton object
 */
object NavX : AHRS(SPI.Port.kMXP) {

    init {
        reset()

    }

    var angleOffset = 0.0

    val correctedAngle: Double
        get() = (-angle + angleOffset).boundDegrees()

}

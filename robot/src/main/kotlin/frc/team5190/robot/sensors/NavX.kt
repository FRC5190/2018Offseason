/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.sensors

import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.SPI
import frc.team5190.lib.geometry.Rotation2d


object NavX : AHRS(SPI.Port.kMXP) {

    init {
        reset()
    }

    var angleOffset = 0.0

    val correctedAngle: Rotation2d
        get() = Rotation2d.fromDegrees(-angle + angleOffset)

}

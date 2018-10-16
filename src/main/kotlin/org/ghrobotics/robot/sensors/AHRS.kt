/*
 * Copyright (c) 2018 FRC Team 5190
 * Ryan Segerstrom, Prateek Machiraju
 */

package org.ghrobotics.robot.sensors

import com.ctre.phoenix.sensors.PigeonIMU
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.I2C
import org.ghrobotics.lib.mathematics.units.Rotation2d
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.Constants

var ahrsSensorType = AHRSSensorType.Pigeon

enum class AHRSSensorType {
    NavX, Pigeon
}

object AHRS : AHRSSensor by create(ahrsSensorType)

private fun create(ahrsSensorType: AHRSSensorType): AHRSSensor = when (ahrsSensorType) {
    AHRSSensorType.NavX -> NavX()
    AHRSSensorType.Pigeon -> Pigeon()
}

interface AHRSSensor : Source<Rotation2d> {
    var angleOffset: Rotation2d
    val correctedAngle: Rotation2d
    fun reset()
    // Source Implementation
    override val value get() = correctedAngle
}

private abstract class AHRSSensorImpl : AHRSSensor {
    protected abstract val sensorYaw: Rotation2d
    override var angleOffset = 0.degree
    override val correctedAngle: Rotation2d get() = (sensorYaw + angleOffset).degree
}

private class Pigeon : AHRSSensorImpl() {
    private val pigeon = PigeonIMU(17)

    init {
        reset()
    }

    override val sensorYaw get() = pigeon.fusedHeading.degree

    override fun reset() {
        pigeon.setYaw(0.0, Constants.kCTRETimeout)
    }
}

private class NavX : AHRSSensorImpl() {
    private val navX = AHRS(I2C.Port.kMXP)

    init {
        reset()
    }

    override val sensorYaw get() = (-navX.fusedHeading).degree

    override fun reset() {
        navX.reset()
    }
}
/*
 * Copyright (c) 2018 FRC Team 5190
 * Ryan Segerstrom, Prateek Machiraju
 */

package org.ghrobotics.robot.sensors

import com.ctre.phoenix.sensors.PigeonIMU
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.I2C
import org.ghrobotics.lib.sensors.AHRSSensor
import org.ghrobotics.lib.sensors.toFalconSensor

var ahrsSensorType = AHRSSensorType.Pigeon

enum class AHRSSensorType {
    NavX, Pigeon
}

object AHRS : AHRSSensor by create(ahrsSensorType)

private fun create(ahrsSensorType: AHRSSensorType) = when (ahrsSensorType) {
    AHRSSensorType.NavX -> AHRS(I2C.Port.kMXP).toFalconSensor()
    AHRSSensorType.Pigeon -> PigeonIMU(17).toFalconSensor()
}
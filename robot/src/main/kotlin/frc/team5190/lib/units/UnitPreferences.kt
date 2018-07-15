/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.units

import frc.team5190.robot.Constants

data class UnitPreferences(var sensorUnitsPerRotation: Int = Constants.kDriveSensorUnitsPerRotation,
                           var radius: Double = Constants.kWheelRadiusInches)

fun preferences(create: UnitPreferences.() -> Unit): UnitPreferences {
    val settings = UnitPreferences()
    create.invoke(settings)
    return settings
}


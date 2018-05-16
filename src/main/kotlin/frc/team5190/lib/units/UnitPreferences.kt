package frc.team5190.lib.units

import frc.team5190.robot.DriveConstants

data class UnitPreferences(var sensorUnitsPerRotation: Int = DriveConstants.SENSOR_UNITS_PER_ROTATION,
                           var radius: Double = DriveConstants.WHEEL_RADIUS)

fun preferences(create: UnitPreferences.() -> Unit): UnitPreferences {
    val settings = UnitPreferences()
    create.invoke(settings)
    return settings
}


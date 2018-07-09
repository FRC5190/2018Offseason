package frc.team5190.lib.units

import frc.team5190.robot.Constants

data class UnitPreferences(var sensorUnitsPerRotation: Int = Constants.DRIVE_SENSOR_UNITS_PER_ROTATION,
                           var radius: Double = Constants.WHEEL_RADIUS)

fun preferences(create: UnitPreferences.() -> Unit): UnitPreferences {
    val settings = UnitPreferences()
    create.invoke(settings)
    return settings
}


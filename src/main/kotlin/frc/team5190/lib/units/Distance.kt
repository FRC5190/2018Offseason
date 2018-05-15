@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package frc.team5190.lib.units

import kotlin.math.roundToInt

interface Distance {
    val feet: Feet
    val inches: Inches
    val nativeUnits: NativeUnits

    operator fun plus (other: Distance) = NativeUnits(this.nativeUnits.value + other.nativeUnits.value, this.nativeUnits.settings)
    operator fun minus (other: Distance) = NativeUnits(this.nativeUnits.value - other.nativeUnits.value, this.nativeUnits.settings)
}

class NativeUnits(val value: Int, internal val settings: UnitPreferences = UnitPreferences()) : Distance {
    override val nativeUnits = this
    override val feet = Feet(value.toDouble() / settings.sensorUnitsPerRotation.toDouble() * (2.0 * Math.PI * settings.wheelRadius) / 12.0)
    override val inches = feet.inches
}

class Inches(val value: Double, settings: UnitPreferences = UnitPreferences()) : Distance {
    override val inches = this
    override val feet = Feet(value / 12.0, settings)
    override val nativeUnits = feet.nativeUnits
}

class Feet(val value: Double, settings: UnitPreferences = UnitPreferences()) : Distance {
    override val feet = this
    override val inches = Inches(value * 12.0)
    override val nativeUnits = NativeUnits((value * 12.0 / (2.0 * Math.PI * settings.wheelRadius) * settings.sensorUnitsPerRotation.toDouble()).roundToInt())
}
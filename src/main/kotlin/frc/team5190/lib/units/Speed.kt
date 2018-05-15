@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package frc.team5190.lib.units

interface Speed {
    val nativeUnitsPer100Ms: NativeUnitsPer100Ms
    val inchesPerSecond: InchesPerSecond
    val feetPerSecond: FeetPerSecond

    operator fun plus (other: Speed) = NativeUnitsPer100Ms(this.nativeUnitsPer100Ms.value + other.nativeUnitsPer100Ms.value, this.nativeUnitsPer100Ms.settings)
    operator fun minus (other: Speed) = NativeUnitsPer100Ms(this.nativeUnitsPer100Ms.value - other.nativeUnitsPer100Ms.value, this.nativeUnitsPer100Ms.settings)
    operator fun unaryMinus() = NativeUnitsPer100Ms(-this.nativeUnitsPer100Ms.value, this.nativeUnitsPer100Ms.settings)
}

class NativeUnitsPer100Ms(val value: Int, internal val settings: UnitPreferences = UnitPreferences()) : Speed {
    override val nativeUnitsPer100Ms = this
    override val feetPerSecond = FeetPerSecond((value.toDouble() / settings.sensorUnitsPerRotation.toDouble() * (2.0 * Math.PI * settings.wheelRadius) / 12.0) * 10.0, settings)
    override val inchesPerSecond = feetPerSecond.inchesPerSecond
}


class InchesPerSecond(val value: Double, settings: UnitPreferences = UnitPreferences()) : Speed {
    override val inchesPerSecond = this
    override val feetPerSecond = FeetPerSecond(value / 12.0, settings)
    override val nativeUnitsPer100Ms = feetPerSecond.nativeUnitsPer100Ms


}

class FeetPerSecond(val value: Double, settings: UnitPreferences = UnitPreferences()) : Speed {
    override val feetPerSecond = this
    override val nativeUnitsPer100Ms = NativeUnitsPer100Ms(((value * 6.0 * settings.sensorUnitsPerRotation) / (10 * Math.PI * settings.wheelRadius)).toInt(), settings)
    override val inchesPerSecond = InchesPerSecond(value * 12.0, settings)
}



@file:Suppress("MemberVisibilityCanBePrivate", "unused", "PropertyName")

package frc.team5190.lib.units

interface Speed {
    val STU: NativeUnitsPer100Ms
    val IPS: InchesPerSecond
    val FPS: FeetPerSecond

    operator fun plus(other: Speed): Speed {
        return NativeUnitsPer100Ms(this.STU.value + other.STU.value, this.STU.settings)
    }

    operator fun minus(other: Speed): Speed {
        return NativeUnitsPer100Ms(this.STU.value - other.STU.value, this.STU.settings)
    }

    operator fun times(other: Speed): Speed {
        return NativeUnitsPer100Ms(this.STU.value * other.STU.value, this.STU.settings)
    }

    operator fun div(other: Speed): Speed {
        return NativeUnitsPer100Ms(this.STU.value / other.STU.value, this.STU.settings)
    }

    operator fun times(scalar: Double): Speed {
        return FeetPerSecond(this.FPS.value * scalar, this.STU.settings)
    }

    operator fun div(scalar: Double): Speed {
        return FeetPerSecond(this.FPS.value / scalar, this.STU.settings)
    }

    operator fun unaryPlus(): Speed {
        return this
    }

    operator fun unaryMinus(): Speed {
        return NativeUnitsPer100Ms(-this.STU.value, this.STU.settings)
    }
}


class NativeUnitsPer100Ms(val value: Int, internal val settings: UnitPreferences = UnitPreferences()) : Speed {
    override val STU = this
    override val FPS = FeetPerSecond((value.toDouble() / settings.sensorUnitsPerRotation.toDouble() * (2.0 * Math.PI * settings.radius) / 12.0) * 10.0, settings)
    override val IPS = FPS.IPS
}


class InchesPerSecond(val value: Double, settings: UnitPreferences = UnitPreferences()) : Speed {
    override val IPS = this
    override val FPS = FeetPerSecond(value / 12.0, settings)
    override val STU = FPS.STU


}

class FeetPerSecond(val value: Double, settings: UnitPreferences = UnitPreferences()) : Speed {
    override val FPS = this
    override val STU = NativeUnitsPer100Ms(((value * 6.0 * settings.sensorUnitsPerRotation) / (10 * Math.PI * settings.radius)).toInt(), settings)
    override val IPS = InchesPerSecond(value * 12.0, settings)
}



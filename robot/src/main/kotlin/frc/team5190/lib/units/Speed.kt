@file:Suppress("MemberVisibilityCanBePrivate", "unused", "PropertyName")

package frc.team5190.lib.units

import kotlin.math.absoluteValue

interface Speed {
    val STU: NativeUnitsPer100Ms
    val IPS: InchesPerSecond
    val FPS: FeetPerSecond

    val absoluteValue: Speed
        get() = NativeUnitsPer100Ms(this.STU.value.absoluteValue, this.STU.settings)

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
    override val STU
        get() = this
    override val FPS
        get() = FeetPerSecond((value.toDouble() / settings.sensorUnitsPerRotation.toDouble() * (2.0 * Math.PI * settings.radius) / 12.0) * 10.0, settings)
    override val IPS
        get() = FPS.IPS
}


class InchesPerSecond(val value: Double, val settings: UnitPreferences = UnitPreferences()) : Speed {
    override val IPS
        get() = this
    override val FPS
        get() = FeetPerSecond(value / 12.0, settings)
    override val STU
        get() = FPS.STU


}

class FeetPerSecond(val value: Double, val settings: UnitPreferences = UnitPreferences()) : Speed {
    override val FPS
        get() = this
    override val STU
        get() = NativeUnitsPer100Ms(((value * 6.0 * settings.sensorUnitsPerRotation) / (10 * Math.PI * settings.radius)).toInt(), settings)
    override val IPS
        get() = InchesPerSecond(value * 12.0, settings)
}



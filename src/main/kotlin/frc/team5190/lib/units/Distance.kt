@file:Suppress("MemberVisibilityCanBePrivate", "unused", "PropertyName")

package frc.team5190.lib.units

import kotlin.math.roundToInt

interface Distance {
    val FT: Feet
    val IN: Inches
    val STU: NativeUnits

    operator fun plus(other: Distance): Distance {
        return NativeUnits(this.STU.value + other.STU.value, this.STU.settings)
    }

    operator fun minus(other: Distance): Distance {
        return NativeUnits(this.STU.value - other.STU.value, this.STU.settings)
    }

    operator fun times(other: Distance): Distance {
        return NativeUnits(this.STU.value * other.STU.value, this.STU.settings)
    }

    operator fun div(other: Distance): Distance {
        return NativeUnits(this.STU.value / other.STU.value, this.STU.settings)
    }

    operator fun times(scalar: Double): Distance {
        return Feet(this.FT.value * scalar, this.STU.settings)
    }

    operator fun div(scalar: Double): Distance {
        return Feet(this.FT.value / scalar, this.STU.settings)
    }

    operator fun unaryPlus(): Distance {
        return this
    }

    operator fun unaryMinus(): Distance {
        return NativeUnits(-this.STU.value, this.STU.settings)
    }
}

class NativeUnits(val value: Int, internal val settings: UnitPreferences = UnitPreferences()) : Distance {
    override val STU = this
    override val FT = Feet(value.toDouble() / settings.sensorUnitsPerRotation.toDouble() * (2.0 * Math.PI * settings.radius) / 12.0)
    override val IN = FT.IN
}

class Inches(val value: Double, settings: UnitPreferences = UnitPreferences()) : Distance {
    override val IN = this
    override val FT = Feet(value / 12.0, settings)
    override val STU = FT.STU
}

class Feet(val value: Double, settings: UnitPreferences = UnitPreferences()) : Distance {
    override val FT = this
    override val IN = Inches(value * 12.0)
    override val STU = NativeUnits((value * 12.0 / (2.0 * Math.PI * settings.radius) * settings.sensorUnitsPerRotation.toDouble()).roundToInt())
}
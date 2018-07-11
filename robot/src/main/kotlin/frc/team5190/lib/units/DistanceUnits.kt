/*
 * FRC Team 5190
 * Green Hope Falcons
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused", "PropertyName")

package frc.team5190.lib.units

import kotlin.math.absoluteValue
import kotlin.math.roundToInt

interface Distance {

    val FT: Double
    val IN: Double
    val STU: Int

    val settings: UnitPreferences

    val absoluteValue: Distance
        get() = NativeUnits(this.STU.absoluteValue, this.settings)


    operator fun plus(other: Distance): Distance {
        return NativeUnits(this.STU + other.STU, this.settings)
    }

    operator fun minus(other: Distance): Distance {
        return NativeUnits(this.STU - other.STU, this.settings)
    }

    operator fun times(other: Distance): Distance {
        return NativeUnits(this.STU * other.STU, this.settings)
    }

    operator fun div(other: Distance): Distance {
        return NativeUnits(this.STU / other.STU, this.settings)
    }

    operator fun times(scalar: Double): Distance {
        return Feet(this.FT * scalar, this.settings)
    }

    operator fun div(scalar: Double): Distance {
        return Feet(this.FT / scalar, this.settings)
    }

    operator fun unaryPlus(): Distance {
        return this
    }

    operator fun unaryMinus(): Distance {
        return NativeUnits(-this.STU, this.settings)
    }
}

class NativeUnits(private val value: Int, override val settings: UnitPreferences = UnitPreferences()) : Distance {
    override val STU
        get() = value
    override val FT
        get() = value.toDouble() / settings.sensorUnitsPerRotation.toDouble() * (2.0 * Math.PI * settings.radius) / 12.0
    override val IN
        get() = FT * 12.0
}

class Inches(private val value: Double, override val settings: UnitPreferences = UnitPreferences()) : Distance {
    override val IN
        get() = value
    override val FT
        get() = value / 12.0
    override val STU
        get() = Feet(FT, settings).STU
}

class Feet(private val value: Double, override val settings: UnitPreferences = UnitPreferences()) : Distance {
    override val FT
        get() = value
    override val IN
        get() = value * 12.0
    override val STU
        get() = (value * 12.0 / (2.0 * Math.PI * settings.radius) * settings.sensorUnitsPerRotation.toDouble()).roundToInt()
}
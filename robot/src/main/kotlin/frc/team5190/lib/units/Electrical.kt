package frc.team5190.lib.units

interface Current {
    val amps: Amps

    operator fun plus(other: Current) = Amps(this.amps.value + other.amps.value)
    operator fun minus(other: Current) = Amps(this.amps.value - other.amps.value)
    operator fun unaryMinus() = Amps(-this.amps.value)
}

class Amps (val value: Int) : Current {
    override val amps = this
}

interface Voltage {
    val volts: Volts

    operator fun plus(other: Voltage) = Volts(this.volts.value + other.volts.value)
    operator fun minus(other: Voltage) = Volts(this.volts.value - other.volts.value)
    operator fun unaryMinus() = Volts(-this.volts.value)
}

class Volts (val value: Double) : Voltage {
    override val volts = this
}



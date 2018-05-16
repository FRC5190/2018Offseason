@file:Suppress("PropertyName")

package frc.team5190.lib.units

interface Time {
    val SEC: Seconds
    val MS: Milliseconds
}

class Seconds(val value: Double) : Time {
    override val SEC = this
    override val MS = Milliseconds((value * 1000).toInt())
}

class Milliseconds(val value: Int) : Time {
    override val MS = this
    override val SEC = Seconds(value / 1000.0)
}
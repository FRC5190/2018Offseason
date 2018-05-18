@file:Suppress("PropertyName")

package frc.team5190.lib.units

interface Time {
    val SEC: Seconds
    val MS: Milliseconds
}

class Seconds(val value: Double) : Time {
    override val SEC
        get() = this
    override val MS
        get() = Milliseconds((value * 1000).toInt())
}

class Milliseconds(val value: Int) : Time {
    override val MS
        get() = this
    override val SEC
        get() = Seconds(value / 1000.0)
}
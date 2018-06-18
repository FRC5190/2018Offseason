@file:Suppress("UNUSED_PARAMETER")

package frc.team5190.lib

import com.ctre.phoenix.CANifier
import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.math.EPSILON
import java.awt.Color
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.absoluteValue

fun CANifier.setLEDOutput(color: Color) = setLEDOutput(color.red, color.green, color.blue)

fun CANifier.setLEDOutput(r: Int, g: Int, b: Int) {
    setLEDOutput(r * (1.0 / 255.0), CANifier.LEDChannel.LEDChannelB)
    setLEDOutput(g * (1.0 / 255.0), CANifier.LEDChannel.LEDChannelA)
    setLEDOutput(b * (1.0 / 255.0), CANifier.LEDChannel.LEDChannelC)
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") {
        String.format("%02x", it)
    }
}

fun commandGroup(create: CommandGroup.() -> Unit): CommandGroup {
    val group = CommandGroup()
    create.invoke(group)
    return group
}

fun Double.enforceBounds(): Double {
    var x = this
    while (x >= PI) x -= (2 * PI)
    while (x < -PI) x += (2 * PI)
    return x
}

infix fun Double.epsilonEquals(other: Double): Boolean {
    return (this - other).absoluteValue < EPSILON
}

infix fun CommandGroup.todo(other: String) = this

infix fun CommandGroup.kthx(other: String) {
    this.start()
}

infix fun Double.cos(other: Double): Double {
    return this * Math.cos(other)
}

infix fun Double.sin(other: Double): Double {
    return this * Math.sin(other)
}
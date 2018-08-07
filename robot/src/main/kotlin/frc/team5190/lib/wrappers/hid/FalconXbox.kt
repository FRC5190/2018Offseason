package frc.team5190.lib.wrappers.hid

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.XboxController

fun xboxController(port: Int, block: FalconHIDBuilder<XboxController>.() -> Unit) = controller(XboxController(port), block)

fun FalconHIDBuilder<XboxController>.button(button: XboxButton, block: FalconHIDButtonBuilder.() -> Unit = {}) = button(button.value, block)
fun FalconHIDBuilder<XboxController>.triggerAxisButton(hand: GenericHID.Hand, threshold: Double = HIDButton.DEFAULT_THRESHOLD, block: FalconHIDButtonBuilder.() -> Unit = {})
        = axisButton(if (hand == GenericHID.Hand.kLeft) 2 else 3, threshold, block)

val FalconHIDBuilder<XboxController>.kBumperLeft
    get() = XboxButton.kBumperLeft
val FalconHIDBuilder<XboxController>.kBumperRight
    get() = XboxButton.kBumperRight
val FalconHIDBuilder<XboxController>.kStickLeft
    get() = XboxButton.kStickLeft
val FalconHIDBuilder<XboxController>.kStickRight
    get() = XboxButton.kStickRight
val FalconHIDBuilder<XboxController>.kA
    get() = XboxButton.kA
val FalconHIDBuilder<XboxController>.kB
    get() = XboxButton.kB
val FalconHIDBuilder<XboxController>.kX
    get() = XboxButton.kX
val FalconHIDBuilder<XboxController>.kY
    get() = XboxButton.kY
val FalconHIDBuilder<XboxController>.kBack
    get() = XboxButton.kBack
val FalconHIDBuilder<XboxController>.kStart
    get() = XboxButton.kStart

enum class XboxButton(val value: Int) {
    kBumperLeft(5),
    kBumperRight(6),
    kStickLeft(9),
    kStickRight(10),
    kA(1),
    kB(2),
    kX(3),
    kY(4),
    kBack(7),
    kStart(8)
}
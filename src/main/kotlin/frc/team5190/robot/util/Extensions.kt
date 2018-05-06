package frc.team5190.robot.util

import com.ctre.phoenix.CANifier
import edu.wpi.first.wpilibj.command.CommandGroup
import java.awt.Color

fun CANifier.setLEDOutput(color: Color) = setLEDOutput(color.red, color.green, color.blue)

fun CANifier.setLEDOutput(r: Int, g: Int, b: Int) {
    setLEDOutput(r * (1.0 / 255.0), CANifier.LEDChannel.LEDChannelB)
    setLEDOutput(g * (1.0 / 255.0), CANifier.LEDChannel.LEDChannelA)
    setLEDOutput(b * (1.0 / 255.0), CANifier.LEDChannel.LEDChannelC)
}

fun commandGroup(create: CommandGroup.() -> Unit): CommandGroup {
    val group = CommandGroup()
    create.invoke(group)
    return group
}



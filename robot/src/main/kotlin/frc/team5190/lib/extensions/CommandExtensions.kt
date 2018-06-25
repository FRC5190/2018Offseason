@file:Suppress("unused")

package frc.team5190.lib.extensions

import edu.wpi.first.wpilibj.command.Command
import edu.wpi.first.wpilibj.command.CommandGroup

fun sequential(block: ArrayList<Command>.() -> Unit): CommandGroup {
    val commandList = ArrayList<Command>()
    val group = CommandGroup()

    block.invoke(commandList)
    commandList.forEach { command -> group.addSequential(command) }

    return group
}

fun parallel(block: ArrayList<Command>.() -> Unit): CommandGroup {
    val commandList = ArrayList<Command>()
    val group = CommandGroup()

    block.invoke(commandList)
    commandList.forEach { command -> group.addParallel(command) }

    return group
}

@Suppress("FunctionName", "UNUSED_PARAMETER")
infix fun CommandGroup.S3ND(other: String) { this.start() }

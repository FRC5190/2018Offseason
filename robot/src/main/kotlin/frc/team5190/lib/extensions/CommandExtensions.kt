/*
 * FRC Team 5190
 * Green Hope Falcons
 */

@file:Suppress("unused")

package frc.team5190.lib.extensions

import edu.wpi.first.wpilibj.command.Command
import edu.wpi.first.wpilibj.command.CommandGroup

fun sequential(block: CommandGroupBuilder.() -> Unit) = object : CommandGroup() {
    init {
        val builder = CommandGroupBuilder(this)
        block(builder)
        for (command in builder.commands) {
            addSequential(command)
        }
    }
}

fun parallel(block: CommandGroupBuilder.() -> Unit) = object : CommandGroup() {
    init {
        val builder = CommandGroupBuilder(this)
        block(builder)
        for (command in builder.commands) {
            addParallel(command)
        }
    }
}

class CommandGroupBuilder(val commandGroup: CommandGroup) {
    val commands = mutableListOf<Command>()

    fun add(command: Command) {
        commands.add(command)
    }

    fun sequential(block: CommandGroupBuilder.() -> Unit) = add(object : CommandGroup() {
        init {
            val builder = CommandGroupBuilder(this)
            block(builder)
            for (command in builder.commands) {
                addSequential(command)
            }
        }
    })

    fun parallel(block: CommandGroupBuilder.() -> Unit) = add(object : CommandGroup() {
        init {
            val builder = CommandGroupBuilder(this)
            block(builder)
            for (command in builder.commands) {
                addParallel(command)
            }
        }
    })
}


@Suppress("FunctionName", "UNUSED_PARAMETER")
infix fun CommandGroup.S3ND(other: String) { this.start() }

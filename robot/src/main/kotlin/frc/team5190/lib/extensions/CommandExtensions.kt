/*
 * FRC Team 5190
 * Green Hope Falcons
 */

@file:Suppress("unused")

package frc.team5190.lib.extensions

import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.CommandGroup
import frc.team5190.lib.commands.ParallelCommandGroup
import frc.team5190.lib.commands.SequentialCommandGroup

// External Extension Helpers

fun sequential(block: CommandGroupBuilder.() -> Unit) = sequential0(block)
fun parallel(block: CommandGroupBuilder.() -> Unit) = parallel0(block)

// Internal Extension Helpers

private fun sequential0(block: CommandGroupBuilder.() -> Unit) = commandGroup(CommandGroupBuilder.BuilderType.SEQUENTIAL,block)
private fun parallel0(block: CommandGroupBuilder.() -> Unit) = commandGroup(CommandGroupBuilder.BuilderType.PARALLEL,block)

private fun commandGroup(type: CommandGroupBuilder.BuilderType, block: CommandGroupBuilder.() -> Unit) = CommandGroupBuilder(type).also { block(it) }.build()

// Builders

class CommandGroupBuilder(private val type: BuilderType) {
    enum class BuilderType { SEQUENTIAL, PARALLEL }

    private val commands = mutableListOf<Command>()

    fun sequential(block: CommandGroupBuilder.() -> Unit) = sequential0(block).also { it.unaryPlus() }
    fun parallel(block: CommandGroupBuilder.() -> Unit) = parallel0(block).also { it.unaryPlus() }

    operator fun Command.unaryPlus() = commands.add(this)

    fun build() = when (type) {
        BuilderType.SEQUENTIAL -> SequentialCommandGroup(commands)
        BuilderType.PARALLEL -> ParallelCommandGroup(commands)
    }
}


@Suppress("FunctionName", "UNUSED_PARAMETER")
infix fun CommandGroup.S3ND(other: String) {
    this.start()
}

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

fun sequential(
        finishCondition: suspend () -> Boolean = { false },
        block: CommandGroupBuilder.() -> Unit
) = sequential0(finishCondition, block)

fun parallel(
        finishCondition: suspend () -> Boolean = { false },
        block: CommandGroupBuilder.() -> Unit
) = parallel0(finishCondition, block)

// Internal Extension Helpers

private fun sequential0(
        finishCondition: suspend () -> Boolean,
        block: CommandGroupBuilder.() -> Unit
) = commandGroup(CommandGroupBuilder.BuilderType.SEQUENTIAL, finishCondition, block)

private fun parallel0(
        finishCondition: suspend () -> Boolean,
        block: CommandGroupBuilder.() -> Unit
) = commandGroup(CommandGroupBuilder.BuilderType.PARALLEL, finishCondition, block)

private fun commandGroup(type: CommandGroupBuilder.BuilderType, finishCondition: suspend () -> Boolean, block: CommandGroupBuilder.() -> Unit): CommandGroup {
    val builder = CommandGroupBuilder(finishCondition, type)
    block(builder)
    return builder.build()
}

// Builders

class CommandGroupBuilder(private val finishCondition: suspend () -> Boolean, private val type: BuilderType) {
    enum class BuilderType { SEQUENTIAL, PARALLEL }

    private val commands = mutableListOf<Command>()

    fun sequential(
            finishCondition: suspend () -> Boolean = { false },
            block: CommandGroupBuilder.() -> Unit
    ) = +sequential0(finishCondition, block)

    fun parallel(
            finishCondition: suspend () -> Boolean = { false },
            block: CommandGroupBuilder.() -> Unit
    ) = +parallel0(finishCondition, block)

    operator fun Command.unaryPlus() = commands.add(this)

    fun build() = when (type) {
        BuilderType.SEQUENTIAL -> object : SequentialCommandGroup(commands) {
            override suspend fun isFinished() = super.isFinished() || finishCondition()
        }
        BuilderType.PARALLEL -> object : ParallelCommandGroup(commands) {
            override suspend fun isFinished() = super.isFinished() || finishCondition()
        }
    }
}


@Suppress("FunctionName", "UNUSED_PARAMETER")
suspend infix fun CommandGroup.S3ND(other: String) {
    this.start()
}

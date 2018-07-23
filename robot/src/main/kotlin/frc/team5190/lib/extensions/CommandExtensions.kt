/*
 * FRC Team 5190
 * Green Hope Falcons
 */

@file:Suppress("unused")

package frc.team5190.lib.extensions

import frc.team5190.lib.commands.*

// External Extension Helpers

fun sequential(
        finishCondition: Condition = Condition.FALSE,
        block: CommandGroupBuilder.() -> Unit
) = sequential0(finishCondition, block)

fun parallel(
        finishCondition: Condition = Condition.FALSE,
        block: CommandGroupBuilder.() -> Unit
) = parallel0(finishCondition, block)

// Internal Extension Helpers

private fun sequential0(
        finishCondition: Condition,
        block: CommandGroupBuilder.() -> Unit
) = commandGroup(CommandGroupBuilder.BuilderType.SEQUENTIAL, finishCondition, block)

private fun parallel0(
        finishCondition: Condition,
        block: CommandGroupBuilder.() -> Unit
) = commandGroup(CommandGroupBuilder.BuilderType.PARALLEL, finishCondition, block)

private fun commandGroup(type: CommandGroupBuilder.BuilderType, finishCondition: Condition, block: CommandGroupBuilder.() -> Unit): CommandGroup {
    val builder = CommandGroupBuilder(finishCondition, type)
    block(builder)
    return builder.build()
}

// Builders

class CommandGroupBuilder(private val finishCondition: Condition, private val type: BuilderType) {
    enum class BuilderType { SEQUENTIAL, PARALLEL }

    private val commands = mutableListOf<Command>()

    fun sequential(
            finishCondition: Condition = Condition.FALSE,
            block: CommandGroupBuilder.() -> Unit
    ) = +sequential0(finishCondition, block)

    fun parallel(
            finishCondition: Condition = Condition.FALSE,
            block: CommandGroupBuilder.() -> Unit
    ) = +parallel0(finishCondition, block)

    operator fun Command.unaryPlus() = commands.add(this)

    fun build() = when (type) {
        BuilderType.SEQUENTIAL -> object : SequentialCommandGroup(commands) {
            init {
                finishCondition += this@CommandGroupBuilder.finishCondition
            }
        }
        BuilderType.PARALLEL -> object : ParallelCommandGroup(commands) {
            init {
                finishCondition += this@CommandGroupBuilder.finishCondition
            }
        }
    }
}


@Suppress("FunctionName", "UNUSED_PARAMETER")
suspend infix fun CommandGroup.S3ND(other: String) {
    this.start()
}

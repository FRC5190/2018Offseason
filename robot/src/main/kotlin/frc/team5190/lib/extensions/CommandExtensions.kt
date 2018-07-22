/*
 * FRC Team 5190
 * Green Hope Falcons
 */

@file:Suppress("unused")

package frc.team5190.lib.extensions

import edu.wpi.first.wpilibj.command.Command
import edu.wpi.first.wpilibj.command.CommandGroup

fun sequential(block: CommandGroupBuilder.() -> Unit) = sequential0(block)
fun parallel(block: CommandGroupBuilder.() -> Unit) = parallel0(block)

private fun sequential0(block: CommandGroupBuilder.() -> Unit) = commandGroup(CommandGroupBuilder.BuilderType.SEQUENTIAL, block)
private fun parallel0(block: CommandGroupBuilder.() -> Unit) = commandGroup(CommandGroupBuilder.BuilderType.PARALLEL, block)

fun commandGroup(type: CommandGroupBuilder.BuilderType, block: CommandGroupBuilder.() -> Unit): CommandGroup {
    val builder = CommandGroupBuilder(type)
    block(builder)
    return builder.build()
}

class CommandGroupBuilder(private val type: BuilderType) {
    enum class BuilderType { SEQUENTIAL, PARALLEL }

    private val commands = mutableListOf<Command>()

    fun sequential(block: CommandGroupBuilder.() -> Unit) = +sequential0(block)
    fun parallel(block: CommandGroupBuilder.() -> Unit) = +parallel0(block)

    operator fun Command.unaryPlus() = commands.add(this)

    fun build() = object : CommandGroup() {
        init {
            for (command in commands) {
                when (type) {
                    BuilderType.SEQUENTIAL -> addSequential(command)
                    BuilderType.PARALLEL -> addParallel(command)
                }
            }
        }
    }
}


@Suppress("FunctionName", "UNUSED_PARAMETER")
infix fun CommandGroup.S3ND(other: String) {
    this.start()
}

package frc.team5190.lib.commands

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.sendBlocking
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import java.util.concurrent.TimeUnit

object CommandHandler {

    private val commandContext = newFixedThreadPoolContext(2, "Command Context")

    /**
     * Stores the currently running tasks
     */
    private val tasks = mutableListOf<CommandTaskImpl>()

    private sealed class CommandEvent {
        class StartEvent(val command: Command) : CommandEvent()
        class StopCommandEvent(val command: Command) : CommandEvent()
        class StopEvent(val command: CommandTaskImpl, val startDefault: (Subsystem) -> Boolean) : CommandEvent()
    }

    private val commandActor = actor<CommandEvent>(context = commandContext, capacity = Channel.UNLIMITED) {
        for (event in channel) {
            handleEvent(event)
        }
    }

    private suspend fun handleEvent(event: CommandEvent) {
        when (event) {
            is CommandEvent.StartEvent -> {
                val command = event.command
                val subsystems = command.requiredSubsystems
                // Free up required subsystems so we can start the command
                val commandsToStop = tasks.filter { task -> task.subsystems.any { subsystem -> subsystems.contains(subsystem) } }.toSet()
                // Stop the tasks that require the subsystems we need and start default commands for subsystems we don't need
                commandsToStop.forEach { task -> handleEvent(CommandEvent.StopEvent(task) { subsystem -> !subsystems.contains(subsystem) }) }
                // Start the command
                val task = CommandTaskImpl(command, subsystems.toList())
                tasks.add(task)
                task.initialize()
            }
            is CommandEvent.StopCommandEvent -> {
                val task = tasks.find { it.command == event.command } ?: return
                handleEvent(CommandEvent.StopEvent(task) { true })
            }
            is CommandEvent.StopEvent -> {
                val command = event.command
                // Stop and dispose command
                command.dispose()
                tasks.remove(command)
                // Start default commands
                val subsystems = command.subsystems.filter { event.startDefault(it) }
                for (subsystem in subsystems) {
                    val default = subsystem.defaultCommand
                    if (default != null) handleEvent(CommandEvent.StartEvent(default))
                }
            }
        }
    }

    fun start(command: Command) {
        // Check if all subsystems are registered
        for (subsystem in command.requiredSubsystems) {
            if (!SubsystemHandler.isRegistered(subsystem)) throw IllegalArgumentException("A command required a subsystem that hasnt been registered! Subsystem: ${subsystem.name} ${subsystem::class.java.simpleName} Command: ${command::class.java.simpleName}")
        }
        commandActor.sendBlocking(CommandEvent.StartEvent(command))
    }

    fun stop(command: Command) = commandActor.sendBlocking(CommandEvent.StopCommandEvent(command))

    private open class CommandTaskImpl(command: Command, val subsystems: List<Subsystem>) : CommandTask(command) {
        override suspend fun stop() = stop(command)
    }

    abstract class CommandTask(val command: Command) {
        private val commandMutex = Mutex()
        private var updater: Job? = null
        private var finishedNormally = false

        private var started = false

        suspend fun initialize() = commandMutex.withLock {
            started = true
            command.commandState = Command.CommandState.BAKING
            command.initialize()
            command.exposedCondition.invokeOnceOnCompletion {
                finishedNormally = true
                launch(commandContext) { stop() } // Stop the command early
            }
            val frequency = command.updateFrequency
            if (frequency == 0) return@withLock
            if (frequency < 0) throw IllegalArgumentException("Command frequency cannot be negative!")

            val timeBetweenUpdate = TimeUnit.SECONDS.toNanos(1) / frequency
            updater = launch(context = commandContext) {
                // Stores when the next update should happen
                var nextNS = System.nanoTime() + timeBetweenUpdate
                while (isActive) {
                    try {
                        if (command.isFinished()) {
                            finishedNormally = true
                            // stop command if finished
                            stop()
                            return@launch
                        }
                        commandMutex.withLock {
                            if (!isActive) return@launch
                            command.execute()
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                    val delayNeeded = nextNS - System.nanoTime()
                    nextNS += timeBetweenUpdate
                    delay(delayNeeded, TimeUnit.NANOSECONDS)
                }
            }
        }

        suspend fun dispose() = commandMutex.withLock {
            if (!started) return
            command.commandState = if (finishedNormally) Command.CommandState.BAKED else Command.CommandState.BURNT
            updater?.cancel()
            updater = null
            command.dispose()
            command.completionHandler.invokeCompletionListeners()
        }

        abstract suspend fun stop()
    }
}
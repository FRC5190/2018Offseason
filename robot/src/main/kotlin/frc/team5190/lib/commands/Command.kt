package frc.team5190.lib.commands

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

object CommandHandler {

    private val tasks = mutableMapOf<Subsystem, CommandTask>()

    private interface CommandEvent
    private class StartEvent(val command: Command) : CommandEvent
    private class StopCommandEvent(val command: Command) : CommandEvent
    private class StopEvent(val command: CommandTask, val startDefault: (Subsystem) -> Boolean) : CommandEvent

    private val commandActor = actor<CommandEvent> {
        for (event in channel) {
            handleEvent(event)
        }
    }

    private suspend fun handleEvent(event: CommandEvent) {
        when (event) {
            is StartEvent -> {
                val command = event.command
                val subsystems = command.requiredSubsystems
                // Free up required subsystems so we can start the command
                val commandsToStop = tasks.filterKeys { subsystems.contains(it) }.values.toSet()
                commandsToStop.forEach { handleEvent(StopEvent(it) { !subsystems.contains(it) }) }
                // Start the command
                val task = CommandTask(command)
                for (subsystem in subsystems) {
                    tasks[subsystem] = task
                }
                task.init()
            }
            is StopCommandEvent -> {
                val task = tasks.values.find { it.command == event.command } ?: return
                handleEvent(StopEvent(task) { true })
            }
            is StopEvent -> {
                val command = event.command
                command.dispose()
                val subsystems = tasks.filterValues { it == command }.keys
                for (subsystem in subsystems) {
                    tasks.remove(subsystem)
                    if (event.startDefault(subsystem)) {
                        val default = subsystem.defaultCommand
                        if (default != null) handleEvent(StartEvent(default))
                    }
                }
            }
        }
    }

    suspend fun start(command: Command) = commandActor.send(StartEvent(command))
    suspend fun stop(command: Command) = commandActor.send(StopCommandEvent(command))

    private class CommandTask(val command: Command) {
        private lateinit var updater: Job

        suspend fun init() {
            command.initialize()
            updater = launch {
                val frequency = command.updateFrequency
                val timeBetweenUpdate = TimeUnit.SECONDS.toNanos(1) / frequency

                // Stores when the next update should happen
                var nextNS = System.nanoTime() + timeBetweenUpdate
                while (isActive) {
                        try {
                            if(command.isFinished()){
                                // stop command if finished
                                stop(command)
                                return@launch
                            }
                            command.execute()
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    val delayNeeded = nextNS - System.nanoTime()
                    nextNS += timeBetweenUpdate
                    delay(delayNeeded, TimeUnit.NANOSECONDS)
                }
            }
        }

        suspend fun dispose() {
            updater.cancelAndJoin()
            command.dispose()
        }
    }
}


abstract class Command(updateFrequency: Int = 50) {
    var updateFrequency = updateFrequency
        protected set

    internal val requiredSubsystems = mutableListOf<Subsystem>()

    protected operator fun Subsystem.unaryPlus() = require(this)
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun require(subsystem: Subsystem) = requiredSubsystems.add(subsystem)

    open suspend fun initialize() {}
    open suspend fun execute() {}
    open suspend fun dispose() {}

    open suspend fun isFinished() = false

    suspend fun start() = CommandHandler.start(this)
    suspend fun stop() = CommandHandler.stop(this)
}
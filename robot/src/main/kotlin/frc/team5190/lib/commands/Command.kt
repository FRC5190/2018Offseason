package frc.team5190.lib.commands

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

object CommandHandler {

    private val tasks = mutableMapOf<Subsystem, SubsystemCommandTask>()

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
                val task = SubsystemCommandTask(command)
                for (subsystem in subsystems) {
                    tasks[subsystem] = task
                }
                task.initialize()
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

    private class SubsystemCommandTask(command: Command) : CommandTask(command) {
        override suspend fun stop() = stop(command)
    }

    abstract class CommandTask(val command: Command) {
        private lateinit var updater: Job
        private lateinit var finishHandle: DisposableHandle
        private var isFinished: Boolean? = null

        suspend fun initialize() {
            command.didComplete = false
            command.didFinish = false
            finishHandle = command.exposedCondition.invokeOnCompletion {
                stop() // Stop the command early
            }
            command.initialize()
            updater = launch {
                val frequency = command.updateFrequency
                val timeBetweenUpdate = TimeUnit.SECONDS.toNanos(1) / frequency

                // Stores when the next update should happen
                var nextNS = System.nanoTime() + timeBetweenUpdate
                while (isActive) {
                    try {
                        if (command.isFinished()) {
                            isFinished = true
                            // stop command if finished
                            stop()
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
            val isFinished = this.isFinished ?: command.isFinished()
            updater.cancelAndJoin()
            command.didComplete = true
            command.didFinish = isFinished
            finishHandle.dispose()
            command.dispose()
            for (completionListener in command.completionListeners.toList()) {
                completionListener(command)
            }
        }

        abstract suspend fun stop()
    }
}


abstract class Command(updateFrequency: Int = DEFAULT_FREQUENCY) {
    companion object {
        const val DEFAULT_FREQUENCY = 50
    }

    var updateFrequency = updateFrequency
        protected set

    internal open val requiredSubsystems: List<Subsystem> = mutableListOf()
    internal val completionListeners = CopyOnWriteArrayList<suspend (Command) -> Unit>()

    protected val finishCondition = CommandCondition(Condition.FALSE)
    internal val exposedCondition: Condition
        get() = finishCondition

    /**
     * Is true when the command was running and has stopped
     */
    var didComplete = false
        internal set

    /**
     * Is true when the command didn't force end
     */
    var didFinish = false
        internal set

    /**
     * Is true when all the finish conditions are met
     */
    suspend fun isFinished() = finishCondition.isMet()

    // Little cheat so you don't have to reassign finishCondition every time you modify it
    protected class CommandCondition(currentCondition: Condition) : Condition() {
        private val listener: suspend (Condition) -> Unit = {
            for (completionListener in completionListeners.toList()) {
                completionListener(it)
            }
        }
        private var handle = currentCondition.invokeOnCompletion(listener)
        private var currentCondition = currentCondition
            set(value) {
                // update handle to new condition
                handle.dispose()
                handle = value.invokeOnCompletion(listener)
                field = value
            }

        override suspend fun isMet() = currentCondition.isMet()
        /**
         * Shortcut for the or operator
         */
        operator fun plusAssign(condition: Condition) {
            currentCondition = currentCondition or condition
        }
    }

    protected operator fun Subsystem.unaryPlus() = requires(this)
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun requires(subsystem: Subsystem) = (requiredSubsystems as MutableList).add(subsystem)

    open suspend fun initialize() {}
    open suspend fun execute() {}
    open suspend fun dispose() {}

    suspend fun start() = CommandHandler.start(this)
    suspend fun stop() = CommandHandler.stop(this)

    fun invokeOnCompletion(block: suspend (Command) -> Unit): DisposableHandle {
        completionListeners.add(block)
        return object : DisposableHandle {
            override fun dispose() {
                completionListeners.remove(block)
            }
        }
    }
}

abstract class CommandGroup(commands: List<Command>) : Command() {
    protected val commands = commands.map { GroupCommandTask(it) }
    override val requiredSubsystems = commands.map { it.requiredSubsystems }.flatten()

    private val actorMutex = Mutex()
    private var started = false
    protected val activeCommands = mutableListOf<GroupCommandTask>()

    private interface GroupEvent
    private object StartEvent : GroupEvent
    private class FinishEvent(val task: GroupCommandTask) : GroupEvent

    private val groupActor = actor<GroupEvent>(capacity = Channel.UNLIMITED, start = CoroutineStart.LAZY) {
        actorMutex.withLock {
            for (event in channel) {
                handleEvent(event)
            }
        }
    }

    private suspend fun handleEvent(event: GroupEvent) {
        when (event) {
            is StartEvent -> {
                handleStartEvent()
                started = true
            }
            is FinishEvent -> {
                val task = event.task
                // Discard if the command somehow gets removed twice
                if (!activeCommands.contains(task)) return
                task.dispose()
                activeCommands.remove(task)
                if (activeCommands.isEmpty()) {
                    // Stop early since the command is finished
                    stop()
                    return
                }
                handleFinishEvent()
            }
        }
    }

    init {
        finishCondition += condition { started && activeCommands.isEmpty() }
    }

    protected abstract suspend fun handleStartEvent()
    protected open suspend fun handleFinishEvent() {}

    override suspend fun initialize() {
        // Send the first event to the actor (this starts the thread and commands)
        groupActor.send(StartEvent)
    }

    override suspend fun dispose() {
        groupActor.close()
        // Wait for the actor is release lock (cheat for detecting when actor finishes)
        actorMutex.withLock {
            for (activeCommand in activeCommands) {
                activeCommand.dispose()
            }
            activeCommands.clear()
        }
    }

    protected inner class GroupCommandTask(command: Command) : CommandHandler.CommandTask(command) {
        override suspend fun stop() = groupActor.send(FinishEvent(this))
    }
}

open class ParallelCommandGroup(commands: List<Command>) : CommandGroup(commands) {
    override suspend fun handleStartEvent() {
        activeCommands += commands
        // Start all commands so they run in parallel
        for (activeCommand in activeCommands) {
            activeCommand.initialize()
        }
    }
}

open class SequentialCommandGroup(commands: List<Command>) : CommandGroup(commands) {
    override suspend fun handleStartEvent() {
        activeCommands += commands
        startNextCommand() // Start only the first command
    }

    override suspend fun handleFinishEvent() = startNextCommand() // Start next command
    /**
     * Starts only the top command (each time a command finishes it gets auto removed from this list)
     */
    private suspend fun startNextCommand() = activeCommands.first().initialize()
}
package frc.team5190.lib.commands

import frc.team5190.lib.utils.CompletionCallback
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.DisposableHandle
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock

abstract class CommandGroup(private val commands: List<Command>) : Command() {

    companion object {
        private val commandGroupContext = newFixedThreadPoolContext(2, "Command Group")
    }

    protected lateinit var commandTasks: List<GroupCommandTask>
        private set
    override val requiredSubsystems = commands.map { it.requiredSubsystems }.flatten()

    /**
     * Subsystems that are currently being used somewhere
     */
    private val reservedSubsystems = mutableMapOf<Subsystem, GroupCommandTask>()
    private val reservedSubsystemQueue = mutableListOf<GroupCommandTask>()
    private var parentCommandGroup: CommandGroup? = null

    private val actorMutex = Mutex()
    private var started = false
    private lateinit var startDeferred: CompletableDeferred<Unit>
    protected val activeCommands = mutableListOf<GroupCommandTask>()

    private sealed class GroupEvent {
        object StartEvent : GroupEvent()
        class ReserveEvent(val command: GroupCommandTask) : GroupEvent()
        class ReserveFinishEvent(val command: GroupCommandTask) : GroupEvent()
        class FinishEvent(val task: GroupCommandTask) : GroupEvent()
    }

    private lateinit var groupActor: SendChannel<GroupEvent>

    private suspend fun handleEvent(event: GroupEvent) {
        when (event) {
            is GroupEvent.StartEvent -> {
                if (commands.isEmpty()) {
                    started = true
                    groupCondition.invoke() // End early since there is no commands in this group
                } else {
                    handleStartEvent()
                    started = true
                }
            }
            is GroupEvent.FinishEvent -> {
                val task = event.task
                // Discard if the command somehow gets removed twice
                if (!activeCommands.contains(task)) return
                task.dispose()
                activeCommands.remove(task)
                if (activeCommands.isEmpty()) {
                    // Stop early since the command is finished
                    groupCondition.invoke()
                    return
                }
                task.reserveCallback()
                handleFinishEvent()
            }
            is GroupEvent.ReserveEvent -> {
                val task = event.command
                val command = task.command
                if (command is CommandGroup) {
                    // Command Groups dont need this
                    command.parentCommandGroup = this
                    task.initialize()
                    return
                }
                val subsystemsNeeded = command.requiredSubsystems
                if (reservedSubsystems.keys.any { subsystemsNeeded.contains(it) }) {
                    // Subsystem is currently in use, delay the start
                    reservedSubsystemQueue += task
                    println("[Command Group] Command ${command::class.java.simpleName} was delayed since it requires a subsystem that already being used in the command group tree")
                    return
                }
                // Subsystems are free, start the task
                startReserved(task)
            }
            is GroupEvent.ReserveFinishEvent -> {
                val task = event.command
                // remove reserved subsystems from map
                reservedSubsystems.filterValues { it == task }.forEach { reservedSubsystems.remove(it.key) }
                // find commands that can run
                while (true) {
                    val nextCommand = reservedSubsystemQueue.find { nextTask ->
                        reservedSubsystems.keys.none { nextTask.command.requiredSubsystems.contains(it) }
                    } ?: return
                    // Next command found, remove from queue and run it
                    reservedSubsystemQueue.remove(nextCommand)
                    startReserved(nextCommand)
                }
            }
        }
    }

    private suspend fun startReserved(task: GroupCommandTask) {
        val subsystemsNeeded = task.command.requiredSubsystems
        subsystemsNeeded.forEach { reservedSubsystems[it] = task }
        task.reserveCallback = {
            groupActor.send(GroupEvent.ReserveFinishEvent(task))
        }
        task.initialize()
    }

    private inner class GroupCondition : Condition() {
        fun invoke() = invokeCompletionListeners()
        override fun isMet() = started && activeCommands.isEmpty()
    }

    private val groupCondition = GroupCondition()

    init {
        updateFrequency = 0
        finishCondition += groupCondition
    }

    protected abstract suspend fun handleStartEvent()
    protected open suspend fun handleFinishEvent() {}

    protected suspend fun reserve(command: GroupCommandTask) {
        // Send reserve request to base command group
        val parent = parentCommandGroup
        if (parent != null) parent.reserve(command)
        else groupActor.send(GroupEvent.ReserveEvent(command))
    }

    override suspend fun initialize() {
        // Start this group
        started = false
        startDeferred = CompletableDeferred()
        commandTasks = commands.map { GroupCommandTask(it) }
        groupActor = actor(context = commandGroupContext, capacity = Channel.UNLIMITED) {
            actorMutex.withLock {
                startDeferred.complete(Unit)
                for (event in channel) {
                    handleEvent(event)
                }
            }
        }
        groupActor.send(GroupEvent.StartEvent)
    }

    override suspend fun dispose() {
        groupActor.close()
        // Wait for the actor to start and gain priority (Because we don't want to stop actor before it starts)
        startDeferred.await()
        // Wait for the actor is release lock (cheat for detecting when actor finishes)
        actorMutex.withLock {
            for (activeCommand in activeCommands) {
                activeCommand.dispose()
            }
            activeCommands.clear()
        }
    }

    protected inner class GroupCommandTask(command: Command) : CommandHandler.CommandTask(command) {
        lateinit var reserveCallback: suspend () -> Unit
        override suspend fun stop() = groupActor.send(GroupEvent.FinishEvent(this))
    }
}

open class ParallelCommandGroup(commands: List<Command>) : CommandGroup(commands) {
    override suspend fun handleStartEvent() {
        activeCommands += commandTasks
        // Start all commands so they run in parallel
        for (activeCommand in activeCommands) {
            reserve(activeCommand)
        }
    }
}

open class SequentialCommandGroup(commands: List<Command>) : CommandGroup(commands) {
    override suspend fun handleStartEvent() {
        activeCommands += commandTasks
        startNextCommand() // Start only the first command
    }

    override suspend fun handleFinishEvent() = startNextCommand() // Start next command
    /**
     * Starts only the top command (each time a command finishes it gets auto removed from this list)
     */
    private suspend fun startNextCommand() = reserve(activeCommands.first())
}
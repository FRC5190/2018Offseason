package frc.team5190.lib.commands

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

    private var parentCommandGroup: CommandGroup? = null
    private lateinit var commandGroupHandler: CommandGroupHandler

    private inner class GroupCondition : Condition() {
        fun invoke() = invokeCompletionListeners()
        override fun not() = TODO("This is never needed")
        override fun isMet() = commandState != CommandState.PREPARED && commandTasks.isEmpty()
    }

    private val groupCondition = GroupCondition()

    init {
        updateFrequency = 0
        finishCondition += groupCondition
    }

    protected abstract suspend fun handleStartEvent()
    protected open suspend fun handleFinishEvent() {}

    override suspend fun initialize() {
        commandGroupHandler = if (parentCommandGroup != null) NestedCommandGroupHandler() else BaseCommandGroupHandler()

        // Start this group
        commandTasks = commands.map { GroupCommandTask(this, it) }
        commandGroupHandler.start()
        handleStartEvent()
    }

    override suspend fun dispose() = commandGroupHandler.dispose()

    protected suspend fun start(task: GroupCommandTask) = commandGroupHandler.startCommand(task)

    protected inner class GroupCommandTask(val group: CommandGroup, command: Command) : CommandHandler.CommandTask(command) {
        override suspend fun stop() = commandGroupHandler.commandFinishCallback(this)
    }

    private interface CommandGroupHandler {
        suspend fun start()
        suspend fun dispose()
        suspend fun commandFinishCallback(task: GroupCommandTask)
        suspend fun startCommand(task: GroupCommandTask)
    }

    private inner class NestedCommandGroupHandler : CommandGroupHandler {
        private val parentHandler = parentCommandGroup!!.commandGroupHandler

        override suspend fun start() = Unit
        override suspend fun dispose() = Unit
        override suspend fun commandFinishCallback(task: GroupCommandTask) = parentHandler.commandFinishCallback(task)
        override suspend fun startCommand(task: GroupCommandTask) = parentHandler.startCommand(task)
    }

    private sealed class GroupEvent {
        class StartTask(val task: GroupCommandTask) : GroupEvent()
        class FinishTask(val task: GroupCommandTask) : GroupEvent()
        object DestroyTask : GroupEvent()
    }

    private inner class BaseCommandGroupHandler : CommandGroupHandler {
        private lateinit var groupActor: SendChannel<GroupEvent>
        private val actorMutex = Mutex()

        private val activeCommands = mutableListOf<GroupCommandTask>()
        private val runningCommands = mutableListOf<GroupCommandTask>()
        private val queuedCommands = mutableListOf<GroupCommandTask>()

        private var destroyed = false

        override suspend fun start() {
            groupActor = actor(context = commandGroupContext, capacity = Channel.UNLIMITED) {
                actorMutex.withLock {
                    for (event in channel) {
                        if (destroyed && activeCommands.isEmpty()) return@withLock // exit since its done cleaning up
                        handleEvent(event)
                    }
                }
            }
        }

        private suspend fun handleEvent(event: GroupEvent) {
            //println("EVENT: ${event::class.java.simpleName}")
            when (event) {
                is GroupEvent.StartTask -> {
                    val task = event.task
                    if(runningCommands.contains(task)) {
                        println("[Command Group] Command ${task.command::class.java.simpleName} is already running, discarding...")
                        return
                    }
                    if (task.command is CommandGroup) {
                        runningCommands+= task
                        task.command.parentCommandGroup = this@CommandGroup
                        task.initialize()
                        return
                    }
                    val canStart = canStart(task)
                    if (!canStart) {
                        queuedCommands += task
                        println("[Command Group] Command ${task.command::class.java.simpleName} was delayed since it requires a subsystem that already being used in the command group tree")
                        return
                    }
                    activeCommands += task
                    task.initialize()
                }
                is GroupEvent.FinishTask -> {
                    val task = event.task
                    runningCommands -= task
                    task.dispose()
                    task.group.commandTasks -= task
                    activeCommands -= task
                    if (destroyed) {
                        closeIfFinished()
                        return // ignore
                    }
                    // Find queued commands that can run
                    var nextTask: GroupCommandTask?
                    do {
                        nextTask = queuedCommands.find { canStart(it) }
                        if (nextTask != null) {
                            queuedCommands -= nextTask
                            handleEvent(GroupEvent.StartTask(nextTask))
                        }
                    } while (nextTask != null)
                    if (task.group.commandTasks.isEmpty()) {
                        task.group.groupCondition.invoke()
                        return // command group finished
                    }
                    task.group.handleFinishEvent()
                }
                is GroupEvent.DestroyTask -> {
                    destroyed = true
                    // signal current tasks to dispose
                    activeCommands.forEach { it.stop() }
                    closeIfFinished()
                }
            }
        }

        private fun closeIfFinished() {
            if(activeCommands.isEmpty()) groupActor.close() // close up
        }

        private fun canStart(task: GroupCommandTask): Boolean {
            val usedSubsystems = activeCommands.map { it.command.requiredSubsystems }.flatten()
            val neededSubsystems = task.command.requiredSubsystems
            return usedSubsystems.none { neededSubsystems.contains(it) }
        }

        override suspend fun dispose() {
            groupActor.send(GroupEvent.DestroyTask)
            actorMutex.withLock {
                assert(activeCommands.isEmpty()) { "Command Group failed to clean up" }
            }
            groupActor.close()
        }

        override suspend fun commandFinishCallback(task: GroupCommandTask) = groupActor.send(GroupEvent.FinishTask(task))
        override suspend fun startCommand(task: GroupCommandTask) = groupActor.send(GroupEvent.StartTask(task))
    }
}

open class ParallelCommandGroup(commands: List<Command>) : CommandGroup(commands) {
    override suspend fun handleStartEvent() {
        // Start all commands so they run in parallel
        val tasksToStart = commandTasks.toList()
        tasksToStart.forEach { start(it) }
    }
}

open class SequentialCommandGroup(commands: List<Command>) : CommandGroup(commands) {
    private lateinit var taskIterator: Iterator<GroupCommandTask>
    override suspend fun handleStartEvent() {
        taskIterator = commandTasks.iterator()
        startNextCommand() // Start only the first command
    }

    override suspend fun handleFinishEvent() = startNextCommand() // Start next command

    private suspend fun startNextCommand() {
        if(taskIterator.hasNext()) start(taskIterator.next())
    }
}
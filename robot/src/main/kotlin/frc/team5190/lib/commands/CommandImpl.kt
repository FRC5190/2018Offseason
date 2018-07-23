package frc.team5190.lib.commands

import java.util.concurrent.TimeUnit

open class TimeoutCommand(private val timeout: Long, private val unit: TimeUnit = TimeUnit.SECONDS) : Command() {
    private var startTime = 0L

    init {
        finishCondition += condition { System.nanoTime() - startTime >= unit.toNanos(timeout) }
    }

    override suspend fun initialize() {
        super.initialize()
        startTime = System.nanoTime()
    }
}

open class InstantCommand : Command() {
    init {
        finishCondition += Condition.TRUE
    }
}

class InstantRunnableCommand(private val runnable: suspend () -> Unit) : InstantCommand() {
    override suspend fun initialize() {
        super.initialize()
        runnable()
    }
}

class PeriodicRunnableCommand(
        private val runnable: suspend () -> Unit,
        exitCondition: Condition,
        updateFrequency: Int = Command.DEFAULT_FREQUENCY
) : Command(updateFrequency) {
    init {
        finishCondition += exitCondition
    }

    override suspend fun execute() = runnable()
}
package frc.team5190.lib.commands

import kotlinx.coroutines.experimental.*
import java.util.concurrent.TimeUnit

open class TimeoutCommand(private val timeout: Long, private val unit: TimeUnit = TimeUnit.SECONDS) : Command() {

    companion object {
        private val timeoutContext = newFixedThreadPoolContext(1, "Timeout Command")
    }

    val timeoutCondition = TimeoutCondition()

    inner class TimeoutCondition : Condition() {
        private lateinit var job: Job
        private var startTime = 0L
        private var running = false

        fun start() {
            running = true
            startTime = System.nanoTime()
            job = launch(context = timeoutContext) {
                delay(timeout, unit)
                invokeCompletionListeners()
            }
        }

        fun stop() {
            running = false
            job.cancel()
        }

        override fun not() = TODO("This is never needed")

        override fun isMet() = running && System.nanoTime() - startTime >= unit.toNanos(timeout)
    }

    init {
        updateFrequency = 0
        finishCondition += timeoutCondition
    }

    override suspend fun initialize() {
        super.initialize()
        timeoutCondition.start()
    }

    override suspend fun dispose() {
        super.dispose()
        timeoutCondition.stop()
    }
}

abstract class InstantCommand : Command() {
    init {
        updateFrequency = 0
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
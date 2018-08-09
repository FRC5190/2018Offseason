package frc.team5190.lib.commands

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.util.concurrent.TimeUnit

class DelayCommand(delay: Long, unit: TimeUnit = TimeUnit.SECONDS) : Command() {
    init {
        updateFrequency = 0
        withTimeout(delay, unit)
    }
}

class DelayCondition(var delay: Long, var unit: TimeUnit) : Condition() {

    companion object {
        private val timeoutContext = newSingleThreadContext("Delay Condition")
    }

    private lateinit var job: Job
    private var startTime = 0L
    private var running = false

    fun start() = start(System.nanoTime())
    fun start(startTime: Long) {
        running = true
        this.startTime = startTime
        job = launch(context = timeoutContext) {
            delay(unit.toNanos(delay) - (System.nanoTime() - startTime), TimeUnit.NANOSECONDS)
            invokeCompletionListeners()
        }
    }

    fun stop() {
        running = false
        job.cancel()
    }

    override fun not() = TODO("This is never needed")

    override fun isMet() = running && System.nanoTime() - startTime >= unit.toNanos(delay)
}
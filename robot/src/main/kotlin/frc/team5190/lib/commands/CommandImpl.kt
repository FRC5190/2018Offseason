package frc.team5190.lib.commands

import java.util.concurrent.TimeUnit


open class ConditionCommand(private val condition: suspend () -> Boolean) : Command() {
    override suspend fun isFinished() = condition()
}

open class TimeoutCommand(private val timeout: Long, private val unit: TimeUnit = TimeUnit.SECONDS) : Command() {
    private var startTime = 0L
    override suspend fun initialize() {
        super.initialize()
        startTime = System.nanoTime()
    }

    override suspend fun isFinished() = super.isFinished() ||
            System.nanoTime() - startTime >= unit.toNanos(timeout)
}
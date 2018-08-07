package frc.team5190.lib.commands

import kotlinx.coroutines.experimental.DisposableHandle
import kotlinx.coroutines.experimental.disposeOnCancellation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import java.util.concurrent.CopyOnWriteArrayList

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

    var commandState = CommandState.PREPARED
        internal set

    enum class CommandState(val finished: Boolean) {
        /**
         * Command is ready and hasn't been ran yet
         */
        PREPARED(false),
        /**
         * Command is currently running and hasn't finished
         */
        BAKING(false),
        /**
         * Command completed normally
         */
        BAKED(true),
        /**
         * Command was forced to finish
         */
        BURNT(true)
    }

    /**
     * Is true when all the finish conditions are met
     */
    suspend fun isFinished() = finishCondition.isMet()

    // Little cheat so you don't have to reassign finishCondition every time you modify it
    protected class CommandCondition(currentCondition: Condition) : Condition() {
        private val listener: suspend (Condition) -> Unit = { invokeCompletionListeners() }
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

    protected operator fun Subsystem.unaryPlus() = (requiredSubsystems as MutableList).add(this)

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

    suspend fun await() = suspendCancellableCoroutine<Unit> { cont ->
        cont.disposeOnCancellation(invokeOnCompletion {
            cont.resume(Unit)
        })
    }
}


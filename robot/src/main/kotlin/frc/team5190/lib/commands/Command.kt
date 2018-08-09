package frc.team5190.lib.commands

import frc.team5190.lib.utils.CompletionCallback
import frc.team5190.lib.utils.CompletionHandler
import frc.team5190.lib.utils.CompletionHandlerImpl
import frc.team5190.lib.wrappers.FalconRobotBase
import kotlinx.coroutines.experimental.DisposableHandle
import kotlinx.coroutines.experimental.disposeOnCancellation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

abstract class Command(updateFrequency: Int = DEFAULT_FREQUENCY) : CompletionHandler {
    companion object {
        const val DEFAULT_FREQUENCY = 50
    }

    init {
        if (FalconRobotBase.INSTANCE.initialized) {
            println("[Command} [WARNING] It is not recommended to create commands after the robot has initialized!")
        }
    }

    var updateFrequency = updateFrequency
        protected set

    internal open val requiredSubsystems: List<Subsystem> = mutableListOf()
    internal val completionHandler = CompletionHandlerImpl {}

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
    fun isFinished() = finishCondition.isMet()

    // Little cheat so you don't have to reassign finishCondition every time you modify it
    protected class CommandCondition(private var currentCondition: Condition) : Condition() {
        private val listener: CompletionCallback.() -> Unit = { invokeCompletionListeners() }
        private var handle: DisposableHandle? = null

        override fun invokeOnCompletion(block: CompletionCallback.() -> Unit): DisposableHandle {
            synchronized(listener) {
                if (handle == null) {
                    handle = currentCondition.invokeOnCompletion(listener)
                }
            }
            return super.invokeOnCompletion(block)
        }

        override fun not() = TODO("Um what, this is never needed")

        override fun isMet() = currentCondition.isMet()
        /**
         * Shortcut for the or operator
         */
        operator fun plusAssign(condition: Condition) {
            synchronized(listener) {
                val newCondition = currentCondition or condition
                if (handle != null) {
                    // update handle to new condition
                    throw IllegalStateException("Cannot add condition once a listener has been added")
                    // handle?.dispose()
                    // handle = newCondition.invokeOnCompletion(listener)
                }
                currentCondition = newCondition
            }
        }
    }

    protected operator fun Subsystem.unaryPlus() = (requiredSubsystems as MutableList).add(this)

    open suspend fun initialize() {}
    open suspend fun execute() {}
    open suspend fun dispose() {}

    fun start() = CommandHandler.start(this)

    fun stop() = CommandHandler.stop(this)

    override fun invokeOnCompletion(block: CompletionCallback.() -> Unit) = completionHandler.invokeOnCompletion(block)

    fun withExit(condition: Condition) = also { finishCondition += condition }

    suspend fun await() = suspendCancellableCoroutine<Unit> { cont ->
        cont.disposeOnCancellation(invokeOnceOnCompletion {
            cont.resume(Unit)
        })
    }
}


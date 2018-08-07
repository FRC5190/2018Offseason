package frc.team5190.lib.commands

import frc.team5190.lib.utils.CompletionCallback
import frc.team5190.lib.utils.CompletionHandler
import frc.team5190.lib.utils.CompletionHandlerImpl
import kotlinx.coroutines.experimental.DisposableHandle

open class ConditionCommand(condition: Condition) : Command() {
    init {
        finishCondition += condition
    }
}

fun condition(block: () -> Boolean) = object : Condition() {
    override fun isMet() = block()
}

fun condition(command: Command) = object : Condition() {
    init {
        command.invokeOnCompletion {
            //Signal to parent that it has finished
            invokeCompletionListeners()
        }
    }

    override fun isMet() = command.commandState.finished
}

infix fun Condition.or(block: () -> Boolean) = this or condition(block)
infix fun Condition.and(block: () -> Boolean) = this and condition(block)

infix fun Condition.or(command: Command) = this or condition(command)
infix fun Condition.and(command: Command) = this and condition(command)

infix fun Condition.or(condition: Condition) = conditionGroup(this, condition) { one, two -> one || two }
infix fun Condition.and(condition: Condition) = conditionGroup(this, condition) { one, two -> one && two }

private fun conditionGroup(firstCondition: Condition, secondCondition: Condition, condition: (Boolean, Boolean) -> Boolean) = object : Condition() {
    private val groupMutex = Any()
    private var hasRegisteredListeners = false

    override fun invokeOnCompletion(block: CompletionCallback.() -> Unit): DisposableHandle {
        synchronized(groupMutex) {
            if (!hasRegisteredListeners) {
                hasRegisteredListeners = true
                firstCondition.invokeOnCompletion {
                    if (condition(true, secondCondition.isMet())) invokeCompletionListeners()
                }
                secondCondition.invokeOnCompletion {
                    if (condition(firstCondition.isMet(), true)) invokeCompletionListeners()
                }
            }
        }
        return super.invokeOnCompletion(block)
    }

    override fun isMet() = condition(firstCondition.isMet(), secondCondition.isMet())
}

abstract class Condition : CompletionHandler {
    companion object {
        val FALSE
            get() = condition { false }
        val TRUE
            get() = condition { true }
    }

    private val completeHandler = CompletionHandlerImpl {
        if (isMet()) it()
    }

    protected fun invokeCompletionListeners() = completeHandler.invokeCompletionListeners()
    override fun invokeOnCompletion(block: CompletionCallback.() -> Unit) = completeHandler.invokeOnCompletion(block)

    abstract fun isMet(): Boolean
}
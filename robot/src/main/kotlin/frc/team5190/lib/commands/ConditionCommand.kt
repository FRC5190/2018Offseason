package frc.team5190.lib.commands

import kotlinx.coroutines.experimental.DisposableHandle
import java.util.concurrent.CopyOnWriteArrayList

open class ConditionCommand(condition: Condition) : Command() {
    @Deprecated("Manually create the condition instead", replaceWith = ReplaceWith("condition(block)"))
    constructor(block: suspend () -> Boolean) : this(condition(block))

    init {
        finishCondition += condition
    }
}

fun condition(block: suspend () -> Boolean) = object : Condition() {
    override suspend fun isMet() = block()
}

fun condition(command: Command) = object : Condition() {
    init {
        command.invokeOnCompletion {
            //Signal to parent that it has finished
            invokeCompletionListeners()
        }
    }

    override suspend fun isMet() = command.didComplete
}

infix fun Condition.or(block: suspend () -> Boolean) = this or condition(block)
infix fun Condition.and(block: suspend () -> Boolean) = this and condition(block)

infix fun Condition.or(command: Command) = this or condition(command)
infix fun Condition.and(command: Command) = this and condition(command)

infix fun Condition.or(condition: Condition) = conditionGroup(this, condition) { one, two -> one || two }
infix fun Condition.and(condition: Condition) = conditionGroup(this, condition) { one, two -> one && two }

private fun conditionGroup(firstCondition: Condition, secondCondition: Condition, condition: (Boolean, Boolean) -> Boolean) = object : Condition() {
    init {
        firstCondition.invokeOnCompletion {
            if (condition(true, secondCondition.isMet())) invokeCompletionListeners()
        }
        secondCondition.invokeOnCompletion {
            if (condition(firstCondition.isMet(), true)) invokeCompletionListeners()
        }
    }

    override suspend fun isMet() = condition(firstCondition.isMet(), secondCondition.isMet())
}

abstract class Condition {
    companion object {
        val FALSE
            get() = condition { false }
        val TRUE
            get() = condition { true }
    }

    internal val completionListeners = CopyOnWriteArrayList<suspend (Condition) -> Unit>()

    protected suspend fun invokeCompletionListeners() {
        for (completionListener in completionListeners.toList()) {
            completionListener(this)
        }
    }

    abstract suspend fun isMet(): Boolean

    fun invokeOnCompletion(block: suspend (Condition) -> Unit): DisposableHandle {
        completionListeners.add(block)
        return object : DisposableHandle {
            override fun dispose() {
                completionListeners.remove(block)
            }
        }
    }
}
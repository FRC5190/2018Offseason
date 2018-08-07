package frc.team5190.lib.utils

import kotlinx.coroutines.experimental.DisposableHandle
import java.util.concurrent.CopyOnWriteArrayList

interface CompletionHandler {
    fun invokeOnceOnCompletion(block: CompletionCallback.() -> Unit) = invokeOnCompletion {
        block(this)
        dispose()
    }

    fun invokeOnCompletion(block: CompletionCallback.() -> Unit): DisposableHandle
}

interface CompletionCallback : DisposableHandle

class CompletionHandlerImpl(private val onAdd: (CompletionEntry) -> Unit) : CompletionHandler {

    private val completionListeners = CopyOnWriteArrayList<CompletionEntry>()

    fun invokeCompletionListeners() {
        completionListeners.forEach { it() }
    }

    override fun invokeOnCompletion(block: CompletionCallback.() -> Unit): DisposableHandle {
        val entry = CompletionEntry(block)
        completionListeners.add(entry)
        onAdd(entry)
        return object : DisposableHandle {
            override fun dispose() {
                completionListeners.remove(entry)
            }
        }
    }

    inner class CompletionEntry(private val callback: CompletionCallbackImpl.() -> Unit) {
        operator fun invoke() = callback(CompletionCallbackImpl(this))
    }

    inner class CompletionCallbackImpl(private val entry: CompletionHandlerImpl.CompletionEntry) : CompletionCallback {
        override fun dispose() {
            completionListeners.remove(entry)
        }
    }
}
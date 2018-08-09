package frc.team5190.lib.commands

import frc.team5190.lib.extensions.parallel
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.util.concurrent.TimeUnit

class ParallelSameTest {

    private object FakeSubsystem : Subsystem()

    private class TestCommand(val id: Int) : TimeoutCommand(5, TimeUnit.SECONDS) {
        init {
            +FakeSubsystem
        }

        override suspend fun initialize() {
            super.initialize()
            println("Start #$id")
        }
    }

    @Test
    fun testSameSubsystem() = runBlocking {
        SubsystemHandler.addSubsystem(FakeSubsystem)

        var realStartTime = 0L
        val group = parallel {
            +InstantRunnableCommand { realStartTime = System.currentTimeMillis() }
            sequential {
                +TimeoutCommand(1, TimeUnit.SECONDS)
                +TestCommand(1)
                +TestCommand(3)
            }
            +TestCommand(2)
        }

        group.start()
        group.await()
        val endTime = System.currentTimeMillis()
        println("Took ${(endTime - realStartTime) / 1000.0} seconds")
    }

}
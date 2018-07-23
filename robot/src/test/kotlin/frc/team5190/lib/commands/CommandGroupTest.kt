/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.commands

import frc.team5190.lib.extensions.parallel
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

class CommandGroupTest {
    @Test
    fun testCommandGroup() {
        runBlocking {
            var startTime = System.currentTimeMillis()
            val command = parallel {
                +object : TimeoutCommand(4L) {
                    init {
                        updateFrequency = 100
                    }

                    override suspend fun initialize() {
                        super.initialize()
                        println("PARALLEL 1")
                    }

                    override suspend fun dispose() {
                        println(System.currentTimeMillis() - startTime)
                    }
                }
                sequential {
                    +object : TimeoutCommand(6L) {
                        init {
                            updateFrequency = 100
                        }

                        override suspend fun initialize() {
                            super.initialize()
                            println("PARALLEL 2 SEQ 1")
                        }

                        override suspend fun dispose() {
                            println(System.currentTimeMillis() - startTime)
                        }
                    }
                    +object : TimeoutCommand(6L) {
                        init {
                            updateFrequency = 100
                        }

                        override suspend fun initialize() {
                            super.initialize()
                            println("PARALLEL 2 SEQ 2")
                        }

                        override suspend fun dispose() {
                            println(System.currentTimeMillis() - startTime)
                        }
                    }
                }

            }


            command.start()

            val completableDeferred = CompletableDeferred<Any>()
            command.invokeOnCompletion {
                completableDeferred.complete(Any())
            }
            completableDeferred.await()
        }


    }
}
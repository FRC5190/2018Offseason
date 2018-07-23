/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.commands

import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.extensions.sequential
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

class CommandGroupTest {
    @Test
    fun testCommandGroup() {
        runBlocking {
            var startTime = System.currentTimeMillis()
            parallel { }
            println(System.currentTimeMillis() - startTime)
            startTime = System.currentTimeMillis()
            parallel { }
            println(System.currentTimeMillis() - startTime)

            val command = sequential {
                +InstantRunnableCommand { startTime = System.currentTimeMillis() }
                parallel {
                    +object : TimeoutCommand(4L) {
                        override suspend fun initialize() {
                            super.initialize()
                            println("PARALLEL 1")
                        }

                        override suspend fun dispose() {
                            super.dispose()
                            println(System.currentTimeMillis() - startTime)
                        }
                    }
                    sequential {
                        +object : TimeoutCommand(6L) {
                            override suspend fun initialize() {
                                super.initialize()
                                println("PARALLEL 2 SEQ 1")
                            }

                            override suspend fun dispose() {
                                super.dispose()
                                println(System.currentTimeMillis() - startTime)
                            }
                        }
                        sequential {
                            +object : TimeoutCommand(6L) {
                                override suspend fun initialize() {
                                    super.initialize()
                                    println("PARALLEL 2 SEQ 2 SEQ 1")
                                }

                                override suspend fun dispose() {
                                    super.dispose()
                                    println(System.currentTimeMillis() - startTime)
                                }
                            }
                            +object : TimeoutCommand(6L) {
                                override suspend fun initialize() {
                                    super.initialize()
                                    println("PARALLEL 2 SEQ 2 SEQ 2")
                                }

                                override suspend fun dispose() {
                                    super.dispose()
                                    println(System.currentTimeMillis() - startTime)
                                }
                            }
                        }
                        +object : TimeoutCommand(6L) {
                            override suspend fun initialize() {
                                super.initialize()
                                println("PARALLEL 2 SEQ 3")
                            }

                            override suspend fun dispose() {
                                super.dispose()
                                println(System.currentTimeMillis() - startTime)
                            }
                        }
                    }
                }
            }

            command.start()

            command.await() // Wait for command to finish
        }


    }
}
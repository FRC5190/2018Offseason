package org.ghrobotics.robot.auto

import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout
import org.ghrobotics.lib.commands.DelayCommand
import org.ghrobotics.lib.commands.InstantRunnableCommand
import org.ghrobotics.lib.commands.asObservableFinish
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.utils.observabletype.invokeOnTrue
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class NestedCommandGroupTest {
    @Test
    fun testNestedCommandGroup() {
        var var0: Long = -1L
        var var1: Long = -1L
        var var2: Long = -1L
        var var3: Long = -1L
        var var4: Long = -1L
        var var5: Long = -1L

        var finished = false

        val delayTime = Trajectories.centerStartToLeftSwitch.lastState.t - 0.2
        val kCommandTimingTolerance = 50.0

        val group = sequential {
            +InstantRunnableCommand { var0 = System.currentTimeMillis() }
            parallel {
                // TRAJECTORY
                +DelayCommand(3L, TimeUnit.SECONDS).apply { commandState.asObservableFinish().invokeOnTrue { var1 = System.currentTimeMillis() - var0 } }
                // SUBSYSTEM PRESET
                +DelayCommand(2L, TimeUnit.SECONDS).apply { commandState.asObservableFinish().invokeOnTrue { var2 = System.currentTimeMillis() - var0 } }

                sequential {
                    // INTAKE DELAY
                    +DelayCommand(delayTime).apply { commandState.asObservableFinish().invokeOnTrue { var3 = System.currentTimeMillis() - var0 } }
                    // SHOOT
                    +DelayCommand(200, TimeUnit.MILLISECONDS).apply { commandState.asObservableFinish().invokeOnTrue { var4 = System.currentTimeMillis() - var0 } }
                }
            }
            +InstantRunnableCommand { finished = true }

        }

        runBlocking {
            group.start()
            withTimeout(5, TimeUnit.SECONDS) {
                group.await()
            }
        }

        println(var1)
        println(var2)
        println(var3)
        println(var4)

        Assert.assertEquals(var1.toDouble(), 3000.0, kCommandTimingTolerance)
        Assert.assertEquals(var2.toDouble(), 2000.0, kCommandTimingTolerance)
        Assert.assertEquals(var3.toDouble(), delayTime * 1000, kCommandTimingTolerance)
        Assert.assertEquals(var4.toDouble(), delayTime * 1000 + 200, kCommandTimingTolerance)
        assert(finished)
    }
}

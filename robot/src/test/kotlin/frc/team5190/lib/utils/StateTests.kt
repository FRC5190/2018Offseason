package frc.team5190.lib.utils

import frc.team5190.lib.commands.and
import frc.team5190.lib.commands.or
import org.junit.Test

class StateTests {

    @Test
    fun basicAnd() {
        val one = constState(true)
        val two = constState(true)

        val three = one and two

        assert(three.value)
    }

    @Test
    fun basicOr() {
        val one = constState(true)
        val two = constState(false)

        val three = one or two
        val four = two or one

        assert(three.value)
        assert(four.value)
    }

    @Test
    fun basicNumToBoolean() {
        val one = constState(5.0)
        val two = processedState(one) { it > 2.5 }

        assert(two.value)
    }

    @Test
    fun variableListener() {
        val one = variableState(1.0)
        val two = processedState(one) { it > 2.0 }

        var called = false

        two.invokeWhenTrue {
            called = true
        }

        assert(!called)
        one.value = 3.0
        assert(called)
    }

    @Test
    fun doubleVariableListener(){
        val one = variableState(1.0)
        val two = variableState(5.0)

        val three = processedState(one) { it > 2.0 }
        val four = processedState(two) { it < 2.0 }

        val five = three and four

        var called = false

        five.invokeWhenTrue {
            called = true
        }

        assert(!called)
        one.value = 3.0
        assert(!called)
        one.value = 1.0
        two.value = 1.0
        assert(!called)
        one.value = 3.0
        assert(called)
    }

    @Test
    fun whenListener(){
        val three = variableState(false)

        var called = false

        var handle = three.invokeWhenTrue {
            called = true
        }

        assert(!called)
        handle.dispose()

        handle = three.invokeWhenFalse {
            called = true
        }

        assert(called)
        handle.dispose()
    }

    @Test
    fun valueTest(){
        val one = variableState(false)
        val two = variableState(false)

        val three = one and two

        assert(!one.value)
        assert(!two.value)
        assert(!three.value)

        one.value = true
        assert(one.value)
        assert(!two.value)
        assert(!three.value)

        one.value = false
        two.value = true
        assert(!one.value)
        assert(two.value)
        assert(!three.value)

        one.value = true
        two.value = true
        assert(one.value)
        assert(two.value)
        assert(three.value)
    }

    @Test
    fun invokeOnce() {
        val one = variableState(true)

        var called = false

        one.invokeOnceOnChange { called = true }

        assert(!called)
        one.value = false
        assert(called)
        called = false
        one.value = true
        assert(!called)
    }

    @Test
    fun andInvokeOnce() {
        val one = variableState(false)
        val two = variableState(false)

        val three = one and two

        var called = false

        three.invokeOnceOnChange { called = true }

        assert(!called)
        two.value = false
        one.value = true
        assert(!called)
        one.value = false
        two.value = true
        assert(!called)
        one.value = true
        two.value = true
        assert(called)
        called = false
        one.value = false
        two.value = false
        assert(!called)
    }

    @Test
    fun counterState() {
        var counter = 0

        val one = updatableState(5) { counter++ }
        val two = processedState(one) { it >= 5 }

        var called = false

        two.invokeWhenTrue { called = true }

        Thread.sleep(500)
        assert(!called)
        Thread.sleep(500)
        assert(called)
    }


}
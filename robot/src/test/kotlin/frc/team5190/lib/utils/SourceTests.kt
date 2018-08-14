package frc.team5190.lib.utils

import org.junit.Test

class SourceTests {

    @Test
    fun constTest() {
        val one = constSource(5)

        assert(one.value == 5)
    }

    @Test
    fun variableSourceTest(){
        var value = 5

        val one = variableSource { value }

        assert(one.value == 5)
        value = 2
        assert(one.value == 2)
    }

    @Test
    fun sourceWithProcessingTest(){
        var value = 1

        val one = variableSource { value }
        val two = one.withProcessing { it > 2 }

        assert(!two.value)
        value = 3
        assert(two.value)
    }

    @Test
    fun sourceMapTest(){
        var value = true

        val constOne = 1
        val constTwo = 2

        val one = variableSource { value }
        val two = one.map(constOne, constTwo)

        assert(two.value == constOne)
        value = false
        assert(two.value == constTwo)
    }

    @Test
    fun sourceEqualsTest(){
        var value = false

        val one = variableSource { value }
        val two = one.withEquals(true)

        assert(!two.value)
        value = true
        assert(two.value)
    }

}
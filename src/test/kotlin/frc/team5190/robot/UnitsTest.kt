package frc.team5190.robot

import frc.team5190.lib.math.units.FeetPerSecond
import org.junit.Assert
import org.junit.Test

class UnitsTest {
    @Test
    fun testUnits() {
        println(FeetPerSecond(17.0).STU)
        Assert.assertEquals(Constants.kVDrive * FeetPerSecond(17.0).STU, 1023.0, 5.0)
    }
}
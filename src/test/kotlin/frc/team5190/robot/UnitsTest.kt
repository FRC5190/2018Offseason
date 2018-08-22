package frc.team5190.robot

import frc.team5190.lib.math.units.FeetPerSecond
import frc.team5190.lib.math.units.InchesPerSecond
import frc.team5190.lib.math.units.preferences
import org.junit.Assert
import org.junit.Test

class UnitsTest {
    @Test
    fun testUnits() {
        println(FeetPerSecond(17.0).STU)
        Assert.assertEquals(Constants.kVDrive * FeetPerSecond(17.0).STU, 1023.0, 5.0)

        println(1023 / InchesPerSecond(70.6, preferences { radius = 1.25 / 2 }).STU.toDouble()) // Elevator kV
    }
}
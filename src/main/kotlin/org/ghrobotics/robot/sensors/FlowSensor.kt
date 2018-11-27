package org.ghrobotics.robot.sensors

import edu.wpi.first.wpilibj.I2C
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.units.inch
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.robot.Constants

// CJMCU-110 with i2c interface
object FlowSensor : Source<Translation2d> {

    @Suppress("unused")
    private object RegisterMap {
        const val kProductId            = 0x00
        const val kMotion               = 0x02
        const val kDeltaX               = 0x03
        const val kDeltaY               = 0x04
        const val kSqual                = 0x05
        const val kConfigurationBits    = 0x0A
        const val kMotionClear          = 0x12
        const val kFrameCapture         = 0x13
        const val kMotionBurst          = 0x50
    }

    private val sensor = I2C(I2C.Port.kOnboard, Constants.kFlowSensorI2CId)
    private var sensorPosition = Translation2d()

    init {
        GlobalScope.launch {
            while (isActive) {
                val buffer = ByteArray(4)
                sensor.read(RegisterMap.kMotionBurst, 4, buffer)

                val dx = (buffer[1] / Constants.kFlowSensorTicksPerInch).inch
                val dy = (buffer[2] / Constants.kFlowSensorTicksPerInch).inch

                val delta = Translation2d(dx, dy)
                sensorPosition += delta
            }
        }
    }

    override fun invoke() = sensorPosition
}
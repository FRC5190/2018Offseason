/*
 * Copyright (c) 2018 FRC Team 5190
 * Ryan Segerstrom, Prateek Machiraju
 */

package frc.team5190.robot.sensors

import com.ctre.phoenix.sensors.PigeonIMU
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.I2C
import frc.team5190.lib.math.geometry.Rotation2d
import frc.team5190.robot.Constants
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

var ahrsSensorType = AHRSSensorType.Pigeon

enum class AHRSSensorType {
    NavX, Pigeon
}

object AHRS : AHRSSensor by create(ahrsSensorType)

private fun create(ahrsSensorType: AHRSSensorType): AHRSSensor = when (ahrsSensorType) {
    AHRSSensorType.NavX -> NavX()
    AHRSSensorType.Pigeon -> Pigeon()
}

interface AHRSSensor {
    var angleOffset: Double
    val correctedAngle: Rotation2d
    fun reset()
}

private abstract class AHRSSensorImpl : AHRSSensor {
    protected abstract val sensorYaw: Double
    override var angleOffset = 0.0
    override val correctedAngle: Rotation2d
        get() = Rotation2d.fromDegrees(sensorYaw + angleOffset)
}

private class Pigeon : AHRSSensorImpl() {
    private val pigeon = PigeonIMU(17)
    private val ypr = DoubleArray(3)

    init {
        reset()
        launch {
            while (isActive) {
                pigeon.getYawPitchRoll(ypr)
                delay(20, TimeUnit.MILLISECONDS)
            }
        }
    }

    override val sensorYaw: Double
        get() = ypr[0]

    override fun reset() {
        pigeon.setYaw(0.0, Constants.kCTRETimeout)
    }
}

private class NavX : AHRSSensorImpl() {
    private val navX = AHRS(I2C.Port.kMXP)

    init {
        reset()
    }

    override val sensorYaw: Double
        get() = -navX.angle

    override fun reset() {
        navX.reset()
    }
}
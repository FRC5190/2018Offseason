/*
 * Copyright (c) 2018 FRC Team 5190
 * Ryan Segerstrom, Prateek Machiraju
 */

package frc.team5190.robot.sensors

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.sensors.PigeonIMU
import frc.team5190.lib.math.geometry.Rotation2d
import frc.team5190.robot.Constants
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

// Pigeon IMU
object NavX : PigeonIMU(17) {

    init {
        reset()
        launch {
            while(isActive){
                getYawPitchRoll(ypr)
                delay(20, TimeUnit.MILLISECONDS)
            }
        }
    }

    var angleOffset = 0.0
    private val ypr = DoubleArray(3)

    val correctedAngle: Rotation2d
        get() = Rotation2d.fromDegrees(ypr[0] + angleOffset)

    fun reset(): ErrorCode = setYaw(0.0, Constants.kCTRETimeout)
}
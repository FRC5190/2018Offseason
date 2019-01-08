package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.derivedunits.volt
import org.ghrobotics.lib.mathematics.units.meter
import org.ghrobotics.lib.mathematics.units.millisecond
import org.ghrobotics.lib.wrappers.ctre.FalconSRX
import org.ghrobotics.robot.Constants

class DriveGearbox(
    masterId: Int,
    slaveId: Int,
    inverted: Boolean,
    phase: Boolean
) {
    val master = FalconSRX(masterId, Constants.kDriveNativeUnitModel)
    val slave = FalconSRX(slaveId, Constants.kDriveNativeUnitModel)

    val allMotors = listOf(master, slave)

    init {
        slave.follow(master)

        master.inverted = inverted
        slave.inverted = inverted

        // Configure Encoder
        master.feedbackSensor = FeedbackDevice.QuadEncoder
        master.encoderPhase = phase
        master.sensorPosition = 0.meter

        allMotors.forEach { motor ->
            motor.peakForwardOutput = 1.0
            motor.peakReverseOutput = -1.0

            motor.nominalForwardOutput = 0.0
            motor.nominalReverseOutput = 0.0

            motor.brakeMode = NeutralMode.Brake

            motor.voltageCompensationSaturation = 12.volt
            motor.voltageCompensationEnabled = true

            motor.peakCurrentLimit = 0.amp
            motor.peakCurrentLimitDuration = 0.millisecond
            motor.continuousCurrentLimit = 40.amp
            motor.currentLimitingEnabled = true
        }
    }
}

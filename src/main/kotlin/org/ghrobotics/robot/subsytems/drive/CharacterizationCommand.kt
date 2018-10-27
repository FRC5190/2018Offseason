package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import kotlinx.coroutines.experimental.GlobalScope
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.mathematics.units.derivedunits.feetPerSecond
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants

class CharacterizationCommand : FalconCommand(DriveSubsystem) {
    private var voltage = 0.0

    private var vIntercept = 0.0

    private val linReg = SimpleRegression()
    private val dataPts = mutableListOf<Pair<Double, Double>>()

    private val avgDriveSpd
        get() = (DriveSubsystem.leftVelocity.feetPerSecond.asDouble + DriveSubsystem.rightVelocity.feetPerSecond.asDouble) / 2.0

    override fun CreateCommandScope.create() {
        finishCondition += GlobalScope.updatableValue { voltage > 12.0 }
        executeFrequency = 1
    }

    override suspend fun InitCommandScope.initialize() {
        dataPts.add(voltage to (avgDriveSpd / Constants.kWheelRadius.feet.asDouble))
    }

    override suspend fun execute() {

        if (avgDriveSpd > 0.01) {
            if (vIntercept == 0.0) {
                vIntercept = voltage
            }
            dataPts.add(voltage to (avgDriveSpd / Constants.kWheelRadius.feet.asDouble))
            println("Added Data Point: $voltage V --> $avgDriveSpd radians per second.")
        }

        voltage += 0.25
        DriveSubsystem.set(ControlMode.PercentOutput, voltage / 12.0, voltage / 12.0)
    }

    override suspend fun dispose() {
        dataPts.forEach { linReg.addData(it.first, it.second) }
        println("kV: ${1 / linReg.slope} V per rad/s, kS: $vIntercept V, Linearity: ${linReg.rSquare}")
    }
}
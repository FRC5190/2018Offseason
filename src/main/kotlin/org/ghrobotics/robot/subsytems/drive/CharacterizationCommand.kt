package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import kotlinx.coroutines.experimental.GlobalScope
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.ghrobotics.lib.commands.Command
import org.ghrobotics.lib.mathematics.units.derivedunits.feetPerSecond
import org.ghrobotics.lib.mathematics.units.nativeunits.NativeUnitVelocity
import org.ghrobotics.lib.mathematics.units.nativeunits.fromModel
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants

class CharacterizationCommand : Command(DriveSubsystem) {
    private var outPct = 0.0

    private var vIntercept = 0.0

    private val linReg = SimpleRegression()
    private val dataPts = mutableListOf<Pair<Double, NativeUnitVelocity>>()

    private val avgDriveSpd
        get() = (DriveSubsystem.leftVelocity + DriveSubsystem.rightVelocity) / 2.0

    init {
        _finishCondition += GlobalScope.updatableValue { outPct > 1.0 }
        executeFrequency = 1
    }

    override suspend fun initialize() {
        dataPts.add(outPct to avgDriveSpd.fromModel(Constants.kDriveNativeUnitModel))
    }

    override suspend fun execute() {
        if (avgDriveSpd.feetPerSecond.asDouble > 0.01) {
            if (vIntercept == 0.0) vIntercept = outPct
            dataPts.add(outPct * 1023 to avgDriveSpd.fromModel(Constants.kDriveNativeUnitModel))
            println("Added Data Point: $outPct% --> $avgDriveSpd feet per second.")
        }
        outPct += 0.25 / 12.0
        DriveSubsystem.set(ControlMode.PercentOutput, outPct, outPct)
    }

    override suspend fun dispose() {
        dataPts.forEach { linReg.addData(it.first, it.second.asDouble) }
        println("kV for Talon SRX: ${1 / linReg.slope}, kS: $vIntercept, Linearity: ${linReg.rSquare}")
    }
}
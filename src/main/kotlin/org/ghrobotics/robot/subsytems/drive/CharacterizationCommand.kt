package org.ghrobotics.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue
import org.apache.commons.math3.stat.regression.SimpleRegression

class CharacterizationCommand : org.ghrobotics.lib.commands.Command(DriveSubsystem) {
    private var outPct = 0.0

    private var vIntercept = 0.0

    private val linReg = SimpleRegression()
    private val dataPts = ArrayList<Pair<Double, Double>>()

    private val avgDriveSpd
        get() = (DriveSubsystem.leftVelocity + DriveSubsystem.rightVelocity) / 2.0

    init {
        _finishCondition += UpdatableObservableValue { outPct > 1.0 }
        executeFrequency = 1
    }

    override suspend fun initialize() {
        startTime = System.currentTimeMillis()
        dataPts.add(outPct to avgDriveSpd.FPS)
    }

    override suspend fun execute() {
        if (avgDriveSpd.FPS > 0.01) {
            if (vIntercept == 0.0) vIntercept = outPct
            dataPts.add(outPct * 1023 to avgDriveSpd.STU.toDouble())
            println("Added Data Point: $outPct% --> $avgDriveSpd feet per second.")
        }
        outPct += 0.25 / 12.0
        DriveSubsystem.set(ControlMode.PercentOutput, outPct, outPct)
    }

    override suspend fun dispose() {
        dataPts.forEach { linReg.addData(it.first, it.second) }
        println("kV for Talon SRX: ${1 / linReg.slope}, kS: $vIntercept, Linearity: ${linReg.rSquare}")
    }
}
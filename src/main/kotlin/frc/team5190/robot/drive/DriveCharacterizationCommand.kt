package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command
import org.apache.commons.math3.stat.regression.SimpleRegression

@Suppress("unused")
class DriveCharacterizationCommand : Command() {
    private var outPct = 0.0
    private var startTime = 0L

    private var vIntercept = 0.0

    private val linReg = SimpleRegression()
    private val dataPts = ArrayList<Pair<Double, Double>>()

    private val avgDriveSpd
        get() = (Drive.leftVelocity.feetPerSecond.value + Drive.rightVelocity.feetPerSecond.value) / 2.0

    init {
        requires(Drive)
    }

    override fun initialize() {
        startTime = System.currentTimeMillis()
        dataPts.add(outPct to avgDriveSpd)
    }

    override fun execute() {
        if (startTime % 1000 == 0L) {
            if (avgDriveSpd > 0.01) {
                if (vIntercept == 0.0) vIntercept = outPct
                dataPts.add(outPct to avgDriveSpd)
                println("Added Data Point: $outPct% --> $avgDriveSpd feet per second.")
            }
            outPct += 0.02
        }
        Drive.set(ControlMode.PercentOutput, outPct, outPct)
    }

    override fun end() {
        dataPts.forEach { linReg.addData(it.first, it.second) }
        println("V: ${1 / linReg.slope}, V Intercept: $vIntercept, Linearity: ${linReg.rSquare}")
    }

    override fun isFinished() = outPct > 1.0
}
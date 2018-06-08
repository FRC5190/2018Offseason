package frc.team5190.robot.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command
import org.apache.commons.math3.stat.regression.SimpleRegression

class CharacterizationCommand : Command() {
    private var outPct = 0.0
    private var lastIncrementTime = 0L

    private var vIntercept = 0.0

    private val linReg = SimpleRegression()
    private val dataPts = ArrayList<Pair<Double, Double>>()

    private val avgDriveSpd
        get() = ((DriveSubsystem.leftVelocity + DriveSubsystem.rightVelocity) / 2.0).FPS.value

    init {
        requires(DriveSubsystem)
    }

    override fun initialize() {
        lastIncrementTime = System.currentTimeMillis()
        dataPts.add(outPct to avgDriveSpd)
    }

    override fun execute() {
        if (System.currentTimeMillis() - lastIncrementTime > 1000) {
            lastIncrementTime = System.currentTimeMillis()
            if (avgDriveSpd > 0.01) {
                if (vIntercept == 0.0) vIntercept = outPct
                dataPts.add(outPct to avgDriveSpd)
                println("Added Data Point: $outPct% --> $avgDriveSpd FT per second.")
            }
            outPct += 0.02
        }
        DriveSubsystem.set(ControlMode.PercentOutput, outPct, outPct)
    }

    override fun end() {
        dataPts.forEach { linReg.addData(it.first, it.second) }
        println("V: ${1 / linReg.slope}, V Intercept: $vIntercept, Linearity: ${linReg.rSquare}")
    }

    override fun isFinished() = outPct > 1.0
}
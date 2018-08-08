/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.drive

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import frc.team5190.lib.commands.Command
import frc.team5190.lib.utils.withDeadband
import frc.team5190.lib.wrappers.hid.getRawButton
import frc.team5190.lib.wrappers.hid.getX
import frc.team5190.lib.wrappers.hid.getY
import frc.team5190.lib.wrappers.hid.kX
import frc.team5190.robot.Controls

class ManualDriveCommand : Command() {

    companion object {
        private var deadband = 0.02
        private val speedSource = Controls.mainXbox.getY(GenericHID.Hand.kLeft).withDeadband(deadband)
        private val rotationSource = Controls.mainXbox.getX(GenericHID.Hand.kLeft).withDeadband(deadband)
        private val quickTurnSource = Controls.mainXbox.getRawButton(kX)
    }


    private var stopThreshold = DifferentialDrive.kDefaultQuickStopThreshold
    private var stopAlpha = DifferentialDrive.kDefaultQuickStopAlpha
    private var stopAccumulator = 0.0

    init {
        +DriveSubsystem
    }

    override suspend fun execute() {
        val speed = -speedSource.value
        val rotation = rotationSource.value

        val angularPower: Double
        val overPower: Boolean

        if (quickTurnSource.value) {
            if (Math.abs(speed) < stopThreshold) {
                stopAccumulator = (1 - stopAlpha) * stopAccumulator + stopAlpha * rotation.coerceIn(-1.0, 1.0) * 2.0
            }
            overPower = true
            angularPower = rotation
        } else {
            overPower = false
            angularPower = Math.abs(speed) * rotation - stopAccumulator

            when {
                stopAccumulator > 1 -> stopAccumulator -= 1.0
                stopAccumulator < -1 -> stopAccumulator += 1.0
                else -> stopAccumulator = 0.0
            }
        }

        var leftMotorOutput = speed + angularPower
        var rightMotorOutput = speed - angularPower

        // If rotationVector is overpowered, reduce both outputs to within acceptable range
        if (overPower) {
            when {
                leftMotorOutput > 1.0 -> {
                    rightMotorOutput -= leftMotorOutput - 1.0
                    leftMotorOutput = 1.0
                }
                rightMotorOutput > 1.0 -> {
                    leftMotorOutput -= rightMotorOutput - 1.0
                    rightMotorOutput = 1.0
                }
                leftMotorOutput < -1.0 -> {
                    rightMotorOutput -= leftMotorOutput + 1.0
                    leftMotorOutput = -1.0
                }
                rightMotorOutput < -1.0 -> {
                    leftMotorOutput -= rightMotorOutput + 1.0
                    rightMotorOutput = -1.0
                }
            }
        }

        DriveSubsystem.set(ControlMode.PercentOutput, leftMotorOutput, rightMotorOutput)
    }
}

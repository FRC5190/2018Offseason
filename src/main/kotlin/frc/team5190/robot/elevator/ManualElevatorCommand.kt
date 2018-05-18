package frc.team5190.robot.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.robot.Controls
import frc.team5190.robot.Robot
import frc.team5190.robot.drive.DriveSubsystem

class ManualElevatorCommand : Command() {

    private var triggerState = false
    private var lastPOV = -1

    init {
        requires(DriveSubsystem)
    }

    override fun execute() {
        if (!Robot.INSTANCE.isClimbing) {
            when {
                Controls.getTriggerAxis(GenericHID.Hand.kRight) > 0.5 -> {
                    ElevatorSubsystem.set(ControlMode.PercentOutput, 0.55).also { triggerState = true }
                }
                triggerState -> {
                    ElevatorSubsystem.set(ControlMode.PercentOutput, ElevatorSubsystem.currentPosition.STU.value.toDouble())
                    triggerState = false
                }
            }
            when {
                Controls.getBumper(GenericHID.Hand.kRight) -> {
                    ElevatorSubsystem.set(ControlMode.PercentOutput, 0.55)
                }
                Controls.getBumperReleased(GenericHID.Hand.kRight) -> {
                    ElevatorSubsystem.set(ControlMode.MotionMagic, ElevatorSubsystem.currentPosition.STU.value.toDouble())
                }
            }

            val pov = Controls.pov
            if (lastPOV != pov) {
                when (pov) {
                    0 -> {
                        ElevatorPresetCommand(ElevatorPreset.SCALE)
                    }
                    90 -> {
                        ElevatorPresetCommand(ElevatorPreset.SWITCH)
                    }
                    180 -> {
                        ElevatorPresetCommand(ElevatorPreset.INTAKE)
                    }
                    270 -> {
                        ElevatorPresetCommand(ElevatorPreset.BEHIND)
                    }
                    else -> null
                }?.start().also { lastPOV = pov }
            }
        }
    }
    override fun isFinished() = false
}
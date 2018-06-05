package frc.team5190.robot.elevator

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.commandGroup
import frc.team5190.robot.arm.ArmPosition
import frc.team5190.robot.arm.ArmSubsystem
import frc.team5190.robot.arm.AutoArmCommand

class ElevatorPresetCommand(elevatorPreset: ElevatorPreset) : CommandGroup() {
    init {
        when (elevatorPreset) {
            ElevatorPreset.SWITCH -> {
                addParallel(AutoArmCommand(ArmPosition.MIDDLE))
                addParallel(commandGroup {
                    addSequential(object : AutoElevatorCommand(ElevatorPosition.FIRST_STAGE) {
                        override fun isFinished(): Boolean {
                            return ArmSubsystem.currentPosition.STU.value < ArmPosition.UP.distance.STU.value + 100 ||
                                    ElevatorSubsystem.currentPosition.STU.value < ElevatorPosition.SWITCH.distance.STU.value
                        }
                    })
                    addSequential(AutoElevatorCommand(ElevatorPosition.SWITCH))
                })
            }

            ElevatorPreset.SCALE -> {
                addParallel(AutoArmCommand(ArmPosition.MIDDLE))
                addParallel(AutoElevatorCommand(ElevatorPosition.SCALE_HIGH))
            }

            ElevatorPreset.BEHIND -> {
                addParallel(LidarElevatorCommand())
                addParallel(commandGroup {
                    addSequential(object : AutoArmCommand(ArmPosition.UP) {
                        override fun isFinished() = ElevatorSubsystem.currentPosition.STU.value > ElevatorPosition.FIRST_STAGE.distance.STU.value
                    })
                    addSequential(AutoArmCommand(ArmPosition.BEHIND))
                })
            }

            ElevatorPreset.INTAKE -> {
                addParallel(AutoArmCommand(ArmPosition.DOWN))
                addParallel(commandGroup {
                    addSequential(object : AutoElevatorCommand(ElevatorPosition.FIRST_STAGE) {
                        override fun isFinished() = ArmSubsystem.currentPosition.STU.value < ArmPosition.UP.distance.STU.value + 100
                    })
                    addSequential(AutoElevatorCommand(ElevatorPosition.INTAKE))
                })
            }
        }
    }
}

enum class ElevatorPreset {
    INTAKE,
    SWITCH,
    SCALE,
    BEHIND
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems

import frc.team5190.lib.commands.Condition
import frc.team5190.lib.commands.condition
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.units.NativeUnits
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.arm.ClosedLoopArmCommand
import frc.team5190.robot.subsytems.elevator.ClosedLoopElevatorCommand
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.elevator.LidarElevatorCommand

@Suppress("FunctionName")
fun SubsystemPresetCommand(preset: SubsystemPreset,
                           exitCondition: Condition = condition { false }) = parallel(exitCondition) {
    when (preset) {
        SubsystemPreset.INTAKE -> {
            +ClosedLoopArmCommand(ArmSubsystem.Position.DOWN)
            sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.FSTAGE, condition {
                    ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.INTAKE)
            }
        }

        SubsystemPreset.SWITCH -> {
            +ClosedLoopArmCommand(ArmSubsystem.Position.MIDDLE)
            sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.FSTAGE, condition {
                    ElevatorSubsystem.currentPosition < ElevatorSubsystem.Position.SWITCH.distance
                            || ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.SWITCH)
            }
        }

        SubsystemPreset.SCALE -> {
            +ClosedLoopArmCommand(ArmSubsystem.Position.MIDDLE)
            +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.HIGHSCALE)
        }

        SubsystemPreset.BEHIND -> {
            +LidarElevatorCommand()
            sequential {
                +ClosedLoopArmCommand(ArmSubsystem.Position.UP.distance + NativeUnits(75), condition {
                    ElevatorSubsystem.currentPosition > ElevatorSubsystem.Position.FSTAGE.distance
                })
                +ClosedLoopArmCommand(ArmSubsystem.Position.BEHIND)
            }
        }
    }
}

enum class SubsystemPreset { INTAKE, SWITCH, SCALE, BEHIND }
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems

import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.units.NativeUnits
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.arm.AutoArmCommand
import frc.team5190.robot.subsytems.elevator.AutoElevatorCommand
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.elevator.LidarElevatorCommand

fun SubsystemPresetCommand(preset: SubsystemPreset,
                           exit: suspend () -> Boolean = { false }) = parallel(exit) {
    when (preset) {
        SubsystemPreset.INTAKE -> {
            +AutoArmCommand(ArmSubsystem.Position.DOWN)
            sequential {
                +AutoElevatorCommand(ElevatorSubsystem.Position.FSTAGE) {
                    ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                }
                +AutoElevatorCommand(ElevatorSubsystem.Position.INTAKE)
            }
        }

        SubsystemPreset.SWITCH -> {
            +AutoArmCommand(ArmSubsystem.Position.MIDDLE)
            sequential {
                +AutoElevatorCommand(ElevatorSubsystem.Position.FSTAGE) {
                    ElevatorSubsystem.currentPosition < ElevatorSubsystem.Position.SWITCH.distance
                            || ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                }
                +AutoElevatorCommand(ElevatorSubsystem.Position.SWITCH)
            }
        }

        SubsystemPreset.SCALE -> {
            +AutoArmCommand(ArmSubsystem.Position.MIDDLE)
            +AutoElevatorCommand(ElevatorSubsystem.Position.HIGHSCALE)
        }

        SubsystemPreset.BEHIND -> {
            +LidarElevatorCommand()
            sequential {
                +AutoArmCommand(ArmSubsystem.Position.UP.distance + NativeUnits(75)) {
                    ElevatorSubsystem.currentPosition > ElevatorSubsystem.Position.FSTAGE.distance
                }
                +AutoArmCommand(ArmSubsystem.Position.BEHIND)
            }
        }
    }
}

enum class SubsystemPreset { INTAKE, SWITCH, SCALE, BEHIND }
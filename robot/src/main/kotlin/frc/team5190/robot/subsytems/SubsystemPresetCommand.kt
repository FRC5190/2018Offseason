/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.units.NativeUnits
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.arm.AutoArmCommand
import frc.team5190.robot.subsytems.elevator.AutoElevatorCommand
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.elevator.LidarElevatorCommand

class SubsystemPresetCommand(preset: Preset,
                             private val exit: () -> Boolean) : CommandGroup() {
    init {
        when (preset) {
            Preset.INTAKE -> parallel {
                +AutoArmCommand(ArmSubsystem.Position.DOWN)
                sequential {
                    +AutoElevatorCommand(ElevatorSubsystem.Position.FSTAGE) {
                        ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                    }
                    +AutoElevatorCommand(ElevatorSubsystem.Position.INTAKE)
                }
            }

            Preset.SWITCH -> parallel {
                +AutoArmCommand(ArmSubsystem.Position.MIDDLE)
                sequential {
                    +AutoElevatorCommand(ElevatorSubsystem.Position.FSTAGE) {
                        ElevatorSubsystem.currentPosition < ElevatorSubsystem.Position.SWITCH.distance
                                || ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                    }
                    +AutoElevatorCommand(ElevatorSubsystem.Position.SWITCH)
                }
            }

            Preset.SCALE -> parallel {
                +AutoArmCommand(ArmSubsystem.Position.MIDDLE)
                +AutoElevatorCommand(ElevatorSubsystem.Position.HIGHSCALE)
            }

            Preset.BEHIND -> parallel {
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

    override fun isFinished() = super.isFinished() || exit()

    enum class Preset { INTAKE, SWITCH, SCALE, BEHIND }
}
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
                add(AutoArmCommand(ArmSubsystem.Position.DOWN))
                sequential {
                    add(AutoElevatorCommand(ElevatorSubsystem.Position.FSTAGE) {
                        ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                    })
                    add(AutoElevatorCommand(ElevatorSubsystem.Position.INTAKE))
                }
            }

            Preset.SWITCH -> parallel {
                add(AutoArmCommand(ArmSubsystem.Position.MIDDLE))
                sequential {
                    add(AutoElevatorCommand(ElevatorSubsystem.Position.FSTAGE) {
                        ElevatorSubsystem.currentPosition < ElevatorSubsystem.Position.SWITCH.distance
                                || ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                    })
                    add(AutoElevatorCommand(ElevatorSubsystem.Position.SWITCH))
                }
            }

            Preset.SCALE -> parallel {
                add(AutoArmCommand(ArmSubsystem.Position.MIDDLE))
                add(AutoElevatorCommand(ElevatorSubsystem.Position.HIGHSCALE))
            }

            Preset.BEHIND -> parallel {
                add(LidarElevatorCommand())
                sequential {
                    add(AutoArmCommand(ArmSubsystem.Position.UP.distance + NativeUnits(75)) {
                        ElevatorSubsystem.currentPosition > ElevatorSubsystem.Position.FSTAGE.distance
                    })
                    add(AutoArmCommand(ArmSubsystem.Position.BEHIND))
                }
            }
        }
    }

    override fun isFinished() = super.isFinished() || exit()

    enum class Preset { INTAKE, SWITCH, SCALE, BEHIND }
}
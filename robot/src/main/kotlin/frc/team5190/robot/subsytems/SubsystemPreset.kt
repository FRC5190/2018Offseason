/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems

import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.condition
import frc.team5190.lib.extensions.parallel
import frc.team5190.lib.math.units.NativeUnits
import frc.team5190.lib.wrappers.hid.FalconHIDButtonBuilder
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.arm.ClosedLoopArmCommand
import frc.team5190.robot.subsytems.elevator.ClosedLoopElevatorCommand
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.elevator.LidarElevatorCommand

enum class SubsystemPreset(private val builder: () -> Command) {
    INTAKE({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.Position.DOWN)
            sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.FSTAGE).withExit(condition {
                    ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.INTAKE)
            }
        }
    }),
    SWITCH({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.Position.MIDDLE)
            sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.FSTAGE).withExit(condition {
                    ElevatorSubsystem.currentPosition < ElevatorSubsystem.Position.SWITCH.distance
                            || ArmSubsystem.currentPosition < ArmSubsystem.Position.UP.distance + NativeUnits(100)
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.SWITCH)
            }
        }
    }),
    SCALE({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.Position.MIDDLE)
            +ClosedLoopElevatorCommand(ElevatorSubsystem.Position.HIGHSCALE)
        }
    }),
    BEHIND({
        parallel {
            +LidarElevatorCommand()
            sequential {
                +ClosedLoopArmCommand(ArmSubsystem.Position.UP.distance + NativeUnits(75)).withExit(condition {
                    ElevatorSubsystem.currentPosition > ElevatorSubsystem.Position.FSTAGE.distance
                })
                +ClosedLoopArmCommand(ArmSubsystem.Position.BEHIND)
            }
        }
    });

    val command
        get() = builder()
}

// Smh
fun FalconHIDButtonBuilder.changeOn(preset: SubsystemPreset) = changeOn(preset.command)
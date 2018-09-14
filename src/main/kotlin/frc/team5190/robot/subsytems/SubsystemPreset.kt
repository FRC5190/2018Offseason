/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems

import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.parallel
import frc.team5190.lib.utils.statefulvalue.StatefulValue
import frc.team5190.lib.wrappers.hid.FalconHIDButtonBuilder
import frc.team5190.lib.mathematics.units.NativeUnits
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.arm.ClosedLoopArmCommand
import frc.team5190.robot.subsytems.elevator.ClosedLoopElevatorCommand
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.elevator.LidarElevatorCommand

enum class SubsystemPreset(private val builder: () -> Command) {
    INTAKE({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.kDownPosition)
            sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit(StatefulValue {
                    ArmSubsystem.currentPosition < ArmSubsystem.kUpPosition + NativeUnits(100)
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kIntakePosition)
            }
        }
    }),
    SWITCH({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.kMiddlePosition)
            sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit(StatefulValue {
                    ElevatorSubsystem.currentPosition < ElevatorSubsystem.kSwitchPosition
                            || ArmSubsystem.currentPosition < ArmSubsystem.kUpPosition + NativeUnits(100)
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kSwitchPosition)
            }
        }
    }),
    SCALE({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.kMiddlePosition)
            +ClosedLoopElevatorCommand(ElevatorSubsystem.kHighScalePosition)
        }
    }),
    BEHIND({
        parallel {
            +LidarElevatorCommand()
            sequential {
                +ClosedLoopArmCommand(ArmSubsystem.kUpPosition + NativeUnits(75)).withExit(StatefulValue {
                    ElevatorSubsystem.currentPosition > ElevatorSubsystem.kFirstStagePosition
                })
                +ClosedLoopArmCommand(ArmSubsystem.kBehindPosition)
            }
        }
    });

    val command
        get() = builder()
}

// Smh
fun FalconHIDButtonBuilder.changeOn(preset: SubsystemPreset) = changeOn(preset.command)
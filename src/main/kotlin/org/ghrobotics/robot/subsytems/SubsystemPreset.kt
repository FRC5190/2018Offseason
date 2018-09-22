/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems

import org.ghrobotics.lib.commands.parallel
import org.ghrobotics.lib.mathematics.units.NativeUnits
import org.ghrobotics.lib.wrappers.hid.FalconHIDButtonBuilder
import org.ghrobotics.robot.subsytems.arm.ArmSubsystem
import org.ghrobotics.robot.subsytems.arm.ClosedLoopArmCommand
import org.ghrobotics.robot.subsytems.elevator.ClosedLoopElevatorCommand
import org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem
import org.ghrobotics.robot.subsytems.elevator.LidarElevatorCommand

enum class SubsystemPreset(private val builder: () -> org.ghrobotics.lib.commands.Command) {
    INTAKE({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.kDownPosition)
            sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit(org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue {
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
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit(org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue {
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
            +org.ghrobotics.robot.subsytems.elevator.ClosedLoopElevatorCommand(org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem.kScalePosition)
            sequential {
                +ClosedLoopArmCommand(ArmSubsystem.kUpPosition + NativeUnits(75)).withExit(org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue {
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
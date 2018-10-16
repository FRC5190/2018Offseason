/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems

import kotlinx.coroutines.experimental.GlobalScope
import org.ghrobotics.lib.commands.Command
import org.ghrobotics.lib.commands.parallel
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.mathematics.units.nativeunits.STU
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.lib.wrappers.hid.FalconHIDButtonBuilder
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.subsytems.arm.ArmSubsystem
import org.ghrobotics.robot.subsytems.arm.ClosedLoopArmCommand
import org.ghrobotics.robot.subsytems.elevator.ClosedLoopElevatorCommand
import org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem

enum class SubsystemPreset(private val builder: () -> Command) {
    INTAKE({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.kDownPosition)
            +sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit(GlobalScope.updatableValue {
                    ArmSubsystem.armPosition < ArmSubsystem.kUpPosition + 100.STU.toModel(Constants.kArmNativeUnitModel)
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kIntakePosition)
            }
        }
    }),
    SWITCH({
        parallel {
            +ClosedLoopArmCommand(ArmSubsystem.kMiddlePosition)
            +sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit(GlobalScope.updatableValue {
                    ElevatorSubsystem.elevatorPosition < ElevatorSubsystem.kSwitchPosition
                            || ArmSubsystem.armPosition < ArmSubsystem.kUpPosition + 100.STU.toModel(Constants.kArmNativeUnitModel)
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
            +ClosedLoopElevatorCommand(ElevatorSubsystem.kScalePosition)
            +sequential {
                +ClosedLoopArmCommand(ArmSubsystem.kUpPosition + 75.STU.toModel(Constants.kArmNativeUnitModel)).withExit(
                    GlobalScope.updatableValue {
                        ElevatorSubsystem.elevatorPosition > ElevatorSubsystem.kFirstStagePosition
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
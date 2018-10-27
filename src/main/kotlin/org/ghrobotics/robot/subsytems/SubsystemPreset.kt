/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems

import kotlinx.coroutines.experimental.GlobalScope
import org.ghrobotics.lib.commands.AbstractFalconCommand
import org.ghrobotics.lib.commands.parallel
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.lib.wrappers.hid.FalconHIDButtonBuilder
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.subsytems.arm.ArmSubsystem
import org.ghrobotics.robot.subsytems.arm.ClosedLoopArmCommand
import org.ghrobotics.robot.subsytems.elevator.ClosedLoopElevatorCommand
import org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem

enum class SubsystemPreset(private val builder: () -> AbstractFalconCommand) {
    INTAKE({
        parallel {
            +ClosedLoopArmCommand(Constants.kArmDownPosition)
            +sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit(GlobalScope.updatableValue {
                    ArmSubsystem.armPosition < Constants.kArmUpPosition + Constants.kArmAutoTolerance
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kIntakePosition)
            }
        }
    }),
    SWITCH({
        parallel {
            +ClosedLoopArmCommand(Constants.kArmMiddlePosition)
            +sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit(GlobalScope.updatableValue {
                    ElevatorSubsystem.elevatorPosition < ElevatorSubsystem.kSwitchPosition
                            || ArmSubsystem.armPosition < Constants.kArmUpPosition + Constants.kArmAutoTolerance
                })
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kSwitchPosition)
            }
        }
    }),
    SCALE({
        parallel {
            +ClosedLoopArmCommand(Constants.kArmMiddlePosition)
            +ClosedLoopElevatorCommand(ElevatorSubsystem.kHighScalePosition)
        }
    }),
    BEHIND({
        parallel {
            +ClosedLoopElevatorCommand(ElevatorSubsystem.kScalePosition)
            +sequential {
                +ClosedLoopArmCommand(Constants.kArmUpPosition + Constants.kArmAutoTolerance).withExit(
                    GlobalScope.updatableValue {
                        ElevatorSubsystem.elevatorPosition > ElevatorSubsystem.kFirstStagePosition
                    })
                +ClosedLoopArmCommand(Constants.kArmBehindPosition)
            }
        }
    });

    val command get() = builder()
}

// Smh
fun FalconHIDButtonBuilder.changeOn(preset: SubsystemPreset) = changeOn(preset.command)
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems

import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.commands.parallel
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.wrappers.hid.FalconHIDButtonBuilder
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.subsytems.arm.ArmSubsystem
import org.ghrobotics.robot.subsytems.arm.ClosedLoopArmCommand
import org.ghrobotics.robot.subsytems.elevator.ClosedLoopElevatorCommand
import org.ghrobotics.robot.subsytems.elevator.ElevatorSubsystem

enum class SubsystemPreset(private val builder: () -> FalconCommand) {
    INTAKE({
        parallel {
            +ClosedLoopArmCommand(Constants.kArmDownPosition)
            +sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit {
                    ArmSubsystem.armPosition < Constants.kArmUpPosition + Constants.kArmAutoTolerance
                }
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kIntakePosition)
            }
        }
    }),
    SWITCH({
        parallel {
            +ClosedLoopArmCommand(Constants.kArmMiddlePosition)
            +sequential {
                +ClosedLoopElevatorCommand(ElevatorSubsystem.kFirstStagePosition).withExit {
                    ElevatorSubsystem.elevatorPosition < ElevatorSubsystem.kSwitchPosition ||
                            ArmSubsystem.armPosition < Constants.kArmUpPosition + Constants.kArmAutoTolerance
                }
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
                +ClosedLoopArmCommand(Constants.kArmUpPosition + Constants.kArmAutoTolerance)
                        .withExit { ElevatorSubsystem.elevatorPosition > ElevatorSubsystem.kFirstStagePosition }
                +ClosedLoopArmCommand(Constants.kArmBehindPosition)
            }
        }
    });

    val command get() = builder()
}

// Smh
fun FalconHIDButtonBuilder.changeOn(preset: SubsystemPreset) = changeOn(preset.command)
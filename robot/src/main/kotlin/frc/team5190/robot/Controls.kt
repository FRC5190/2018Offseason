/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.wpilibj.XboxController
import frc.team5190.lib.wrappers.FalconRobotBase
import frc.team5190.robot.subsytems.SubsystemPreset
import frc.team5190.robot.subsytems.SubsystemPresetCommand
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.arm.OpenLoopArmCommand
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.elevator.OpenLoopElevatorCommand
import frc.team5190.robot.subsytems.intake.IntakeCommand
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit
import kotlin.math.pow

object Controls : XboxController(0) {

    private val leftBumperPressed get() = getBumperPressed(Hand.kLeft)
    private val leftBumperReleased get() = getBumperReleased(Hand.kLeft)

    private val rightBumperPressed get() = getBumperPressed(Hand.kRight)
    private val rightBumperReleased get() = getBumperReleased(Hand.kRight)

    init {
        launch {
            var lastElevatorPOV = -1
            var elevatorTriggerState = false
            var intakeTriggerState = false

            val downElevatorCommand = OpenLoopElevatorCommand(-0.4)
            val upElevatorCommand = OpenLoopElevatorCommand(0.4)

            while (true) {
                if(Robot.INSTANCE.currentMode != FalconRobotBase.Mode.TELEOP) {
                    delay(50, TimeUnit.MILLISECONDS)
                    continue
                }
                // ARM
                if (yButtonPressed) {
                    OpenLoopArmCommand(0.5).start()
                } else if (bButtonPressed) {
                    OpenLoopArmCommand(-0.5).start()
                } else if (yButtonReleased || bButtonReleased) {
                    ArmSubsystem.defaultCommand?.start()
                }

                // ELEVATOR
                if (getTriggerAxis(Hand.kRight) > 0.2) {
                    if (!upElevatorCommand.queuedStart) upElevatorCommand.start().also { elevatorTriggerState = true }
                    lastElevatorPOV = -1
                } else if (rightBumperPressed) {
                    if (!downElevatorCommand.queuedStart) downElevatorCommand.start()
                    lastElevatorPOV = -1
                } else if (rightBumperReleased || elevatorTriggerState) {
                    val command = ElevatorSubsystem.defaultCommand
                    if (command != null) if (!command.queuedStart) command.start().also { elevatorTriggerState = false }
                    lastElevatorPOV = -1
                }

                if (pov != lastElevatorPOV) {
                    when (pov) {
                        0 -> SubsystemPresetCommand(SubsystemPreset.SCALE)
                        90 -> SubsystemPresetCommand(SubsystemPreset.SWITCH)
                        180 -> SubsystemPresetCommand(SubsystemPreset.INTAKE)
                        270 -> SubsystemPresetCommand(SubsystemPreset.BEHIND)
                        else -> null
                    }?.also {
                        lastElevatorPOV = pov
                        it.start()
                    }
                }


                // INTAKE
                if (getTriggerAxis(Hand.kLeft) > 0.1) {
                    IntakeCommand(IntakeSubsystem.Direction.OUT, getTriggerAxis(Hand.kLeft).pow(2) * 0.65).start()
                    intakeTriggerState = true
                } else if (leftBumperPressed) {
                    IntakeCommand(IntakeSubsystem.Direction.IN, 1.0).start()
                } else if (leftBumperReleased || intakeTriggerState) {
                    IntakeSubsystem.defaultCommand?.start().also { intakeTriggerState = false }
                }

                delay(20)
            }
        }
    }
}

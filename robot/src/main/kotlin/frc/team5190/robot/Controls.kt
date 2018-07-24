/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.wpilibj.XboxController
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

            while (true) {
                // ARM
                if (yButtonPressed) {
                    OpenLoopArmCommand(0.5).start()
                }
                if (bButtonPressed) {
                    OpenLoopArmCommand(-0.5).start()
                }
                if (yButtonReleased || bButtonReleased) {
                    ArmSubsystem.defaultCommand?.start()
                }


                // ELEVATOR
                if (getTriggerAxis(Hand.kRight) > 0.2) {
                    OpenLoopElevatorCommand(0.4).start().also { elevatorTriggerState = true }
                }
                if (rightBumperPressed) {
                    OpenLoopElevatorCommand(-0.4).start()
                }
                if (rightBumperReleased || elevatorTriggerState) {
                    ElevatorSubsystem.defaultCommand?.start().also { elevatorTriggerState = false }
                }

                if (pov != lastElevatorPOV) {
                    when (pov) {
                        0    -> SubsystemPresetCommand(SubsystemPreset.SCALE)
                        90   -> SubsystemPresetCommand(SubsystemPreset.SWITCH)
                        180  -> SubsystemPresetCommand(SubsystemPreset.INTAKE)
                        270  -> SubsystemPresetCommand(SubsystemPreset.BEHIND)
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
                }
                if (leftBumperPressed) {
                    IntakeCommand(IntakeSubsystem.Direction.IN, 1.0).start()
                }
                if (leftBumperReleased || intakeTriggerState) {
                    IntakeSubsystem.defaultCommand?.start().also { intakeTriggerState = false }
                }

                delay(20)
            }
        }
    }
}

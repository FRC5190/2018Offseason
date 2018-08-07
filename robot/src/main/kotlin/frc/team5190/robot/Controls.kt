/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.wpilibj.GenericHID
import frc.team5190.lib.wrappers.FalconRobotBase
import frc.team5190.lib.wrappers.hid.*
import frc.team5190.robot.subsytems.SubsystemPreset
import frc.team5190.robot.subsytems.SubsystemPresetCommand
import frc.team5190.robot.subsytems.arm.OpenLoopArmCommand
import frc.team5190.robot.subsytems.elevator.OpenLoopElevatorCommand
import frc.team5190.robot.subsytems.intake.IntakeCommand
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit
import kotlin.math.pow

object Controls {
    val mainFalconXbox = xboxController(0) {
        // Arm Controls
        val armUpCommand = OpenLoopArmCommand(0.5)
        val armDownCommand = OpenLoopArmCommand(-0.5)
        button(kY).change(armUpCommand)
        button(kB).change(armDownCommand)

        // Elevator Controls
        val elevatorUpCommand = OpenLoopElevatorCommand(0.4)
        val elevatorDownCommand = OpenLoopElevatorCommand(-0.4)
        triggerAxisButton(GenericHID.Hand.kRight, 0.2).change(elevatorUpCommand)
        button(kBumperRight).change(elevatorDownCommand)

        // Presets
        pov(0).changeOn { SubsystemPresetCommand(SubsystemPreset.SCALE).start() }
        pov(90).changeOn { SubsystemPresetCommand(SubsystemPreset.SWITCH).start() }
        pov(180).changeOn { SubsystemPresetCommand(SubsystemPreset.INTAKE).start() }
        pov(270).changeOn { SubsystemPresetCommand(SubsystemPreset.BEHIND).start() }

        // Intake Controls
        triggerAxisButton(GenericHID.Hand.kLeft, 0.1) {
            change(IntakeCommand(IntakeSubsystem.Direction.OUT, IntakeCommand.HIDIntakeSpeedSource(source) { it.pow(2) * 0.65 }))
        }
        button(kBumperLeft).change(IntakeCommand(IntakeSubsystem.Direction.IN, 1.0))
    }

    // Some shortcuts
    val mainXbox = mainFalconXbox.genericHID

    init {
        launch {
            while (isActive) {
                if (Robot.INSTANCE.currentMode != FalconRobotBase.Mode.TELEOP) {
                    delay(100, TimeUnit.MILLISECONDS)
                    continue
                }
                mainFalconXbox.update()
                delay(20)
            }
        }
    }
}
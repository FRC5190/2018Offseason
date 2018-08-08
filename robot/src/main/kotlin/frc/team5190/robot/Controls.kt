/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot

import edu.wpi.first.wpilibj.GenericHID
import frc.team5190.lib.utils.constSource
import frc.team5190.lib.utils.withProcessing
import frc.team5190.lib.wrappers.hid.*
import frc.team5190.robot.subsytems.SubsystemPreset
import frc.team5190.robot.subsytems.arm.OpenLoopArmCommand
import frc.team5190.robot.subsytems.changeOn
import frc.team5190.robot.subsytems.elevator.OpenLoopElevatorCommand
import frc.team5190.robot.subsytems.intake.IntakeCommand
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import kotlin.math.pow

object Controls {
    val mainXbox = xboxController(0) {
        // Arm Controls
        val armUpCommand = OpenLoopArmCommand(constSource(0.5))
        val armDownCommand = OpenLoopArmCommand(constSource(-0.5))
        button(kY).change(armUpCommand)
        button(kB).change(armDownCommand)

        // Elevator Controls
        val elevatorUpCommand = OpenLoopElevatorCommand(constSource(0.4))
        val elevatorDownCommand = OpenLoopElevatorCommand(constSource(-0.4))
        triggerAxisButton(GenericHID.Hand.kRight, 0.2).change(elevatorUpCommand)
        button(kBumperRight).change(elevatorDownCommand)

        // Presets
        pov(0).changeOn(SubsystemPreset.SCALE)
        pov(90).changeOn(SubsystemPreset.SWITCH)
        pov(180).changeOn(SubsystemPreset.INTAKE)
        pov(270).changeOn(SubsystemPreset.BEHIND)

        // Intake Controls
        triggerAxisButton(GenericHID.Hand.kLeft, 0.1) {
            change(IntakeCommand(IntakeSubsystem.Direction.OUT, source.withProcessing { it.pow(2) * 0.65 }))
        }
        button(kBumperLeft).change(IntakeCommand(IntakeSubsystem.Direction.IN, constSource(1.0)))
    }
}
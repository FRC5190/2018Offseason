/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot

import edu.wpi.first.wpilibj.GenericHID
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.observabletype.ObservableVariable
import org.ghrobotics.lib.wrappers.hid.*
import org.ghrobotics.robot.subsytems.SubsystemPreset
import org.ghrobotics.robot.subsytems.arm.OpenLoopArmCommand
import org.ghrobotics.robot.subsytems.changeOn
import org.ghrobotics.robot.subsytems.climber.ClimberSubsystem
import org.ghrobotics.robot.subsytems.climber.ClosedLoopClimbCommand
import org.ghrobotics.robot.subsytems.climber.OpenLoopClimbCommand
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem
import org.ghrobotics.robot.subsytems.elevator.OpenLoopElevatorCommand
import org.ghrobotics.robot.subsytems.intake.IntakeCommand
import org.ghrobotics.robot.subsytems.intake.IntakeSubsystem
import kotlin.math.pow

object Controls {

    val isClimbing = ObservableVariable(false)

    val mainXbox = xboxController(0) {

        button(kBack).changeOn { isClimbing.value = true }
        button(kStart).changeOn { isClimbing.value = false }

        if (!isClimbing.value) {
            // Arm Controls
            val armUpCommand = OpenLoopArmCommand(Source(0.5))
            val armDownCommand = OpenLoopArmCommand(Source(-0.5))

            button(kY).change(armUpCommand)
            button(kB).change(armDownCommand)

            // Drive Controls
            button(kA).changeOn { DriveSubsystem.lowGear = true }
            button(kA).changeOff { DriveSubsystem.lowGear = false }

            // Elevator Controls
            val elevatorUpCommand = OpenLoopElevatorCommand(Source(0.4))
            val elevatorDownCommand = OpenLoopElevatorCommand(Source(-0.4))

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
            button(kBumperLeft).change(IntakeCommand(IntakeSubsystem.Direction.IN, Source(1.0)))
        } else {

            val climberUpCommand = OpenLoopClimbCommand(Source(0.9))
            val climberDownCommand = OpenLoopClimbCommand(Source(-0.9))

            triggerAxisButton(GenericHID.Hand.kRight, 0.2).change(climberUpCommand)
            button(kBumperRight).change(climberDownCommand)

            // Presets
            pov(90).changeOn(ClosedLoopClimbCommand(ClimberSubsystem.kHighScalePosition))
            pov(180).changeOn(ClosedLoopClimbCommand(ClimberSubsystem.kBottomPosition))
        }
    }
}
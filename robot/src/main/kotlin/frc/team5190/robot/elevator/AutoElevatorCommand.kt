package frc.team5190.robot.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.units.Distance
import frc.team5190.lib.units.Inches
import frc.team5190.robot.Controls
import kotlin.math.absoluteValue

open class AutoElevatorCommand(private val distance: Distance) : Command() {

    constructor(elevatorPosition: ElevatorPosition) : this (elevatorPosition.distance)

    init {
        this.requires(ElevatorSubsystem)
    }

    override fun initialize() {
        ElevatorSubsystem.set(ControlMode.MotionMagic, distance.STU.value.toDouble())
    }

    override fun execute() {
        if (Controls.getBumper(GenericHID.Hand.kRight) || Controls.getTriggerAxis(GenericHID.Hand.kRight) > 0.5) {
            cancel()
        }
    }

    override fun isFinished() = (ElevatorSubsystem.currentPosition - distance).IN.value.absoluteValue < Inches(1.0, ElevatorSubsystem.prefs).value
}
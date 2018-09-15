/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.mathematics.units.Distance
import org.ghrobotics.lib.mathematics.units.Inches
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue
import org.ghrobotics.robot.Constants

class ClosedLoopElevatorCommand(private val distance: Distance? = null) : org.ghrobotics.lib.commands.Command(ElevatorSubsystem) {

    private var targetPosition: Distance = Inches(0.0)

    init {
        if (distance != null) {
            // Only finish command if it has an objective
            _finishCondition += UpdatableObservableValue { (ElevatorSubsystem.currentPosition - targetPosition).absoluteValue < Constants.kElevatorClosedLpTolerance }
        }
    }

    override suspend fun initialize() {
        targetPosition = distance ?: ElevatorSubsystem.currentPosition
        ElevatorSubsystem.set(ControlMode.MotionMagic, targetPosition.STU.toDouble())
    }
}
package org.ghrobotics.robot.subsytems.climber

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.mathematics.units.Distance
import org.ghrobotics.lib.mathematics.units.NativeUnits
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue
import org.ghrobotics.robot.Constants

class ClosedLoopClimbCommand(private val distance: Distance? = null) : org.ghrobotics.lib.commands.Command(ClimberSubsystem) {
    private var targetPosition: Distance = NativeUnits(0)

    init {
        if (distance != null) {
            // Only finish command if it has an objective
            _finishCondition += UpdatableObservableValue { (ClimberSubsystem.currentPosition - targetPosition).absoluteValue < Constants.kClimberClosedLpTolerance }
        }
    }

    override suspend fun initialize() {
        targetPosition = distance ?: ClimberSubsystem.currentPosition
        ClimberSubsystem.set(ControlMode.MotionMagic, targetPosition.STU.toDouble())
    }
}
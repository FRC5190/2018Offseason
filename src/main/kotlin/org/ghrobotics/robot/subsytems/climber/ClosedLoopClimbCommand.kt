package org.ghrobotics.robot.subsytems.climber

import kotlinx.coroutines.experimental.GlobalScope
import org.ghrobotics.lib.commands.Command
import org.ghrobotics.lib.mathematics.units.nativeunits.NativeUnit
import org.ghrobotics.lib.mathematics.units.nativeunits.STU
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants

class ClosedLoopClimbCommand(private val distance: NativeUnit? = null) : Command(ClimberSubsystem) {
    private var targetPosition: NativeUnit = 0.STU

    init {
        if (distance != null) {
            // Only finish command if it has an objective
            _finishCondition += GlobalScope.updatableValue { (ClimberSubsystem.climberPosition - targetPosition).absoluteValue < Constants.kClimberClosedLpTolerance }
        }
    }

    override suspend fun initialize() {
        targetPosition = distance ?: ClimberSubsystem.climberPosition
        ClimberSubsystem.climberPosition = targetPosition
    }
}
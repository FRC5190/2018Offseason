/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.elevator

import kotlinx.coroutines.experimental.GlobalScope
import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.mathematics.units.Length
import org.ghrobotics.lib.mathematics.units.inch
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants

class ClosedLoopElevatorCommand(private val distance: Length? = null) : FalconCommand(ElevatorSubsystem) {

    private var targetPosition = 0.inch

    override fun CreateCommandScope.create() {
        if (distance != null) {
            // Only finish command if it has an objective
            finishCondition += GlobalScope.updatableValue {
                (ElevatorSubsystem.elevatorPosition - targetPosition).absoluteValue < Constants.kElevatorClosedLpTolerance
            }
        }
    }

    override suspend fun InitCommandScope.initialize() {
        targetPosition = distance ?: ElevatorSubsystem.elevatorPosition
        ElevatorSubsystem.elevatorPosition = targetPosition
    }
}
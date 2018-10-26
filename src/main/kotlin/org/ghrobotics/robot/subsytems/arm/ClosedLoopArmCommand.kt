/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.arm

import kotlinx.coroutines.experimental.GlobalScope
import org.ghrobotics.lib.commands.Command
import org.ghrobotics.lib.mathematics.units.Rotation2d
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.utils.observabletype.updatableValue
import org.ghrobotics.robot.Constants

class ClosedLoopArmCommand(private val pos: Rotation2d? = null) : Command(ArmSubsystem) {

    private var targetPosition: Rotation2d = 0.degree

    init {
        if (pos != null) {
            // Only finish command if it has an objective
            _finishCondition += GlobalScope.updatableValue {
                (ArmSubsystem.armPosition - targetPosition).absoluteValue < Constants.kArmClosedLoopTolerance
            }
        }
    }

    override suspend fun initialize() {
        targetPosition = pos ?: ArmSubsystem.armPosition
        ArmSubsystem.armPosition = targetPosition
    }

}
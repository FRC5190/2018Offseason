/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.arm

import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.mathematics.units.Rotation2d
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.robot.Constants

class ClosedLoopArmCommand(private val pos: Rotation2d? = null) : FalconCommand(ArmSubsystem) {

    private var targetPosition: Rotation2d = 0.degree

    override fun CreateCommandScope.create() {
        if (pos != null) {
            // Only finish command if it has an objective
            finishCondition += {
                (ArmSubsystem.armPosition - targetPosition).absoluteValue < Constants.kArmClosedLoopTolerance
            }
        }
    }

    override suspend fun InitCommandScope.initialize() {
        targetPosition = pos ?: ArmSubsystem.armPosition
        ArmSubsystem.armPosition = targetPosition
    }
}
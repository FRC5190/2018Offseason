/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.commands.FalconCommand

class IntakeHoldCommand : FalconCommand(IntakeSubsystem) {
    override suspend fun initialize() {
        IntakeSubsystem.solenoid.set(true)
        IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)
    }
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.utils.DoubleSource


class OpenLoopArmCommand(private val percentOutput: DoubleSource) : org.ghrobotics.lib.commands.Command(ArmSubsystem) {
    override suspend fun execute() = ArmSubsystem.set(ControlMode.PercentOutput, percentOutput.value)
}
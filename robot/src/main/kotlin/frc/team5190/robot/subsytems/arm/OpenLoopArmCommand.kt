/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command


class OpenLoopArmCommand(private val percentOutput: Double) : Command() {
    init {
        +ArmSubsystem
    }

    override suspend fun execute() {
        ArmSubsystem.set(ControlMode.PercentOutput, percentOutput)
    }
}
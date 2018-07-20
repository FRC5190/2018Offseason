/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.arm

import edu.wpi.first.wpilibj.command.Command

class ManualArmCommand : Command() {
    init {
        requires(ArmSubsystem)
    }
    override fun isFinished(): Boolean = false
}
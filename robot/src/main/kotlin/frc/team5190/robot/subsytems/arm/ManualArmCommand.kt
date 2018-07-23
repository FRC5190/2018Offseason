/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.arm

import frc.team5190.lib.commands.Command


class ManualArmCommand : Command() {
    init {
        +ArmSubsystem
    }
}
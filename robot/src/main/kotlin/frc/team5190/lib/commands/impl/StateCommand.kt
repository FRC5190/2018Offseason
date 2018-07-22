/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.commands.impl

import edu.wpi.first.wpilibj.command.Command

class StateCommand(private val condition: () -> Boolean) : Command() {
    override fun isFinished() = condition.invoke()
}
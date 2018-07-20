/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.commands

import edu.wpi.first.wpilibj.command.Command

class PeriodicRunnableCommand(private val runnable: () -> Unit, private val exit: () -> Boolean) : Command() {
    override fun execute() = runnable()
    override fun isFinished() = exit()
}
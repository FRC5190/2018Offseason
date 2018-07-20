/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.commands

import edu.wpi.first.wpilibj.command.InstantCommand

class InstantRunnableCommand(val runnable: () -> Unit) : InstantCommand() {
    override fun initialize() = runnable()
}
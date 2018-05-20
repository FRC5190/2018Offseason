package frc.team5190.lib.util

import edu.wpi.first.wpilibj.command.Command

class StateCommand(private val condition: Boolean) : Command() {
    override fun isFinished() = condition
}
package org.ghrobotics.robot.auto.routines

import org.ghrobotics.lib.commands.BasicCommandGroupBuilder
import org.ghrobotics.lib.commands.InstantRunnableCommand
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.robot.auto.Autonomous
import org.ghrobotics.robot.subsytems.drive.DriveSubsystem

fun autoRoutine(block: BasicCommandGroupBuilder.() -> Unit) = sequential {
    +InstantRunnableCommand {
        println("[AutoRoutine] Starting routine...")
        DriveSubsystem.localization.reset(Autonomous.Config.startingPosition().pose)
    }
    block()
}
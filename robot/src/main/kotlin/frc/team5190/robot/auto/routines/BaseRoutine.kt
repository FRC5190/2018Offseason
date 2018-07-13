/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.robot.Localization
import frc.team5190.robot.NetworkInterface
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.subsytems.drive.DriveSubsystem

abstract class BaseRoutine(startingPosition: Autonomous.StartingPositions) {
    abstract val routine: CommandGroup

    init {
        NetworkInterface.INSTANCE.getEntry("Reset").setBoolean(true)
        DriveSubsystem.resetEncoders()
        Localization.reset(startingPosition.pose)
    }
}
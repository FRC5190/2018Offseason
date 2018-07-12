/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto.routines

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.robot.NetworkInterface

abstract class BaseRoutine {
    abstract val routine: CommandGroup

    init {
        NetworkInterface.kInstance.getEntry("Reset").setBoolean(true)
    }
}
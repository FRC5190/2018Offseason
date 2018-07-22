/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.math.units.Distance
import frc.team5190.robot.Constants

class AutoElevatorCommand(private val distance: Distance,
                          private val exit: () -> Boolean = { false }) : Command() {

    constructor(pos: ElevatorSubsystem.Position, exit: () -> Boolean = { false }) : this(pos.distance, exit)

    init {
        requires(ElevatorSubsystem)
    }

    override suspend fun initialize() = ElevatorSubsystem.set(ControlMode.MotionMagic, distance.STU.toDouble())
    override suspend fun isFinished() = (ElevatorSubsystem.currentPosition - distance).absoluteValue <
            Constants.kElevatorClosedLpTolerance || exit()
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.Condition
import frc.team5190.lib.commands.condition
import frc.team5190.lib.commands.or
import frc.team5190.lib.math.units.Distance
import frc.team5190.robot.Constants

class ClosedLoopElevatorCommand(private val distance: Distance? = null) : Command() {

    constructor(pos: ElevatorSubsystem.Position) : this(pos.distance)

    init {
        +ElevatorSubsystem
        val fixedPos = distance ?: ElevatorSubsystem.currentPosition
        if (distance != null) finishCondition += condition { (ElevatorSubsystem.currentPosition - fixedPos).absoluteValue < Constants.kElevatorClosedLpTolerance }
    }

    override suspend fun initialize() = ElevatorSubsystem.set(ControlMode.MotionMagic, (distance ?: ElevatorSubsystem.currentPosition).STU.toDouble())
}
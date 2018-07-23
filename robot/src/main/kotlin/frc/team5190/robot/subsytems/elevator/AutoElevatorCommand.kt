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

class AutoElevatorCommand(private val distance: Distance,
                          exitCondition: Condition = Condition.FALSE) : Command() {

    constructor(pos: ElevatorSubsystem.Position, exitCondition: Condition = Condition.FALSE) : this(pos.distance, exitCondition)

    init {
        requires(ElevatorSubsystem)
        finishCondition += condition { (ElevatorSubsystem.currentPosition - distance).absoluteValue < Constants.kElevatorClosedLpTolerance } or exitCondition
    }

    override suspend fun initialize() = ElevatorSubsystem.set(ControlMode.MotionMagic, distance.STU.toDouble())
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.condition
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.Inches
import frc.team5190.robot.Constants

class ClosedLoopElevatorCommand(private val distance: Distance? = null) : Command() {

    private var targetPosition: Distance = Inches(0.0)

    init {
        +ElevatorSubsystem
        if (distance != null) {
            // Only finish command if it has an objective
            finishCondition += condition { (ElevatorSubsystem.currentPosition - targetPosition).absoluteValue < Constants.kElevatorClosedLpTolerance }
        }
    }

    override suspend fun initialize() {
        targetPosition = distance ?: ElevatorSubsystem.currentPosition
        ElevatorSubsystem.set(ControlMode.MotionMagic, targetPosition.STU.toDouble())
    }
}
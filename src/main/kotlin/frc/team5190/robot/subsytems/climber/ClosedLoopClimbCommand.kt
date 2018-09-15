package frc.team5190.robot.subsytems.climber

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.mathematics.units.Distance
import frc.team5190.lib.mathematics.units.NativeUnits
import frc.team5190.lib.utils.observabletype.UpdatableObservableValue
import frc.team5190.robot.Constants

class ClosedLoopClimbCommand(private val distance: Distance? = null) : Command(ClimberSubsystem) {
    private var targetPosition: Distance = NativeUnits(0)

    init {
        if (distance != null) {
            // Only finish command if it has an objective
            _finishCondition += UpdatableObservableValue { (ClimberSubsystem.currentPosition - targetPosition).absoluteValue < Constants.kClimberClosedLpTolerance }
        }
    }

    override suspend fun initialize() {
        targetPosition = distance ?: ClimberSubsystem.currentPosition
        ClimberSubsystem.set(ControlMode.MotionMagic, targetPosition.STU.toDouble())
    }
}
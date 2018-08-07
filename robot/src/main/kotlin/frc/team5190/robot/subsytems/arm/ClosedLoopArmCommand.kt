/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.Condition
import frc.team5190.lib.commands.condition
import frc.team5190.lib.commands.or
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.NativeUnits

class ClosedLoopArmCommand(private val pos: Distance? = null,
                           exitCondition: Condition = Condition.FALSE) : Command() {

    constructor(position: ArmSubsystem.Position, exitCondition: Condition = Condition.FALSE) : this(position.distance, exitCondition)

    init {
        +ArmSubsystem
        val fixedPos = pos ?: ArmSubsystem.currentPosition
        finishCondition += exitCondition
        if (pos != null) finishCondition += condition { (ArmSubsystem.currentPosition - fixedPos).absoluteValue < NativeUnits(50) }
    }

    override suspend fun initialize() = ArmSubsystem.set(ControlMode.MotionMagic, (pos ?: ArmSubsystem.currentPosition).STU.toDouble())
}
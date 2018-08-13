/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.commands.condition
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.Inches
import frc.team5190.lib.math.units.NativeUnits

class ClosedLoopArmCommand(private val pos: Distance? = null) : Command() {

    constructor(position: ArmSubsystem.Position) : this(position.distance)

    private var targetPosition: Distance = Inches(0.0)

    init {
        +ArmSubsystem
        if (pos != null) {
            // Only finish command if it has an objective
            finishCondition += condition { (ArmSubsystem.currentPosition - targetPosition).absoluteValue < NativeUnits(50) }
        }
    }

    override suspend fun initialize() {
        targetPosition = pos ?: ArmSubsystem.currentPosition
        ArmSubsystem.set(ControlMode.MotionMagic, targetPosition.STU.toDouble())
    }
}
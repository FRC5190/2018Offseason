/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.math.units.Distance
import frc.team5190.lib.math.units.NativeUnits

class AutoArmCommand(private val pos: Distance,
                     private val exit: () -> Boolean = { false }) : Command() {

    constructor(position: ArmSubsystem.Position, exit: () -> Boolean = { false }) : this(position.distance, exit)

    init {
        requires(ArmSubsystem)
    }
    override suspend fun initialize() = ArmSubsystem.set(ControlMode.MotionMagic, pos.STU.toDouble())
    override suspend fun isFinished() = (ArmSubsystem.currentPosition - pos).absoluteValue < NativeUnits(50) || exit()

}
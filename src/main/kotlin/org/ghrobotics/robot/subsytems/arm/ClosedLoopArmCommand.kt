/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import org.ghrobotics.lib.mathematics.units.Distance
import org.ghrobotics.lib.mathematics.units.Inches
import org.ghrobotics.lib.mathematics.units.NativeUnits
import org.ghrobotics.lib.utils.observabletype.UpdatableObservableValue

class ClosedLoopArmCommand(private val pos: Distance? = null) : org.ghrobotics.lib.commands.Command(ArmSubsystem) {

    private var targetPosition: Distance = Inches(0.0)

    init {
        if (pos != null) {
            // Only finish command if it has an objective
            _finishCondition += UpdatableObservableValue { (ArmSubsystem.currentPosition - targetPosition).absoluteValue < NativeUnits(50) }
        }
    }

    override suspend fun initialize() {
        targetPosition = pos ?: ArmSubsystem.currentPosition
        ArmSubsystem.set(ControlMode.MotionMagic, targetPosition.STU.toDouble())
    }
}
package frc.team5190.robot.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.units.Distance
import frc.team5190.robot.Controls
import kotlin.math.absoluteValue

open class AutoArmCommand(val distance: Distance) : Command() {

    constructor(armPosition: ArmPosition) : this(armPosition.distance)

    init {
        this.requires(ArmSubsystem)
    }

    override fun initialize() {
        ArmSubsystem.set(ControlMode.MotionMagic, distance.STU.value.toDouble())
    }

    override fun execute() {
        if (Controls.yButton || Controls.bButton) {
            cancel()
        }
    }

    override fun isFinished() = (ArmSubsystem.currentPosition - distance).STU.value.absoluteValue < 50
}
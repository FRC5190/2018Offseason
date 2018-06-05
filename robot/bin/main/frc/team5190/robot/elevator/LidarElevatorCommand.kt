package frc.team5190.robot.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command
import frc.team5190.lib.units.Inches
import frc.team5190.lib.util.CircularBuffer
import frc.team5190.robot.intake.IntakeSubsystem
import frc.team5190.robot.sensors.Lidar

class LidarElevatorCommand : Command() {

    init {
        requires(ElevatorSubsystem)
        requires(Lidar)
    }

    private val heightBuffer = CircularBuffer(3)

    override fun execute() {
        // Setpoint is changed dynamically based on the motion of the scale
        if (Lidar.underScale) heightBuffer.add(Inches(Lidar.scaleHeight - 15.0).STU.value.toDouble())

        // Motion magic setpoint
        ElevatorSubsystem.set(ControlMode.MotionMagic,
                if (Lidar.underScale) {
                    heightBuffer.average.coerceIn(ElevatorPosition.FIRST_STAGE.distance.STU.value.toDouble(), ElevatorPosition.SCALE_HIGH.distance.STU.value.toDouble())
                }
                else ElevatorPosition.SCALE.distance.STU.value.toDouble())
    }

    override fun isFinished(): Boolean {
        return !IntakeSubsystem.isCubeIn
                && ElevatorSubsystem.currentPosition.STU.value > (ElevatorPosition.FIRST_STAGE.distance - Inches(1.0, ElevatorSubsystem.prefs)).STU.value
    }


}

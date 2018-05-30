package frc.team5190.robot.sensors

import com.ctre.phoenix.CANifier
import edu.wpi.first.wpilibj.Servo
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.robot.Autonomous
import frc.team5190.robot.ChannelIDs
import frc.team5190.robot.Localization
import frc.team5190.robot.Robot
import jaci.pathfinder.Pathfinder
import openrio.powerup.MatchData
import org.apache.commons.math3.stat.regression.SimpleRegression

object Lidar : Subsystem() {

    private val pwmData = DoubleArray(2)

    private var lidarServo = Servo(ChannelIDs.LIDAR_SERVO)

    private val regressionFunction = SimpleRegression()

    // All in inches
    private const val minScaleHeight = 48.0
    private const val maxScaleHeight = 72.0
    private const val allowedTolerance = 3.0

    var underScale = false
        private set

    var scaleHeight = 0.0
        private set

    private var rawDistance = 0.0

    init {
        // X - Raw Sensor Units
        // Y - Height in Inches
        val data = arrayOf(
                1050.0 to 45.0,
                1500.0 to 55.0,
                1900.0 to 70.0
        )
        data.forEach { regressionFunction.addData(it.first, it.second) }
    }

    // Periodic 50hz loop
    override fun periodic() {
        Canifier.getPWMInput(CANifier.PWMChannel.PWMChannel0, pwmData)

        rawDistance = pwmData[0]
        scaleHeight = regressionFunction.predict(rawDistance)
        underScale = minScaleHeight - allowedTolerance < scaleHeight && scaleHeight < maxScaleHeight + allowedTolerance

        val scaleSide = Autonomous.scaleSide


        var servoAngle = Pathfinder.boundHalfDegrees(Localization.robotPosition.let {
            val scalePosition = 27.0 to 13.5 + (if (scaleSide == MatchData.OwnedSide.LEFT) 1.0 else -1.0) * 6.5
            return@let Math.toDegrees(Math.atan2(scalePosition.first - it.x, scalePosition.second - it.y)) + 180 + Pigeon.correctedAngle
        })

        servoAngle = ((servoAngle + 90) % 360) - 90.0
        lidarServo.angle = if (Robot.INSTANCE.isOperatorControl) 90.0 else servoAngle

    }

    override fun initDefaultCommand() {}
}
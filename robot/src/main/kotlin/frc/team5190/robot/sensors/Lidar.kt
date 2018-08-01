/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.sensors

import com.ctre.phoenix.CANifier
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.Servo
import frc.team5190.lib.math.geometry.Rotation2d
import frc.team5190.robot.Constants
import frc.team5190.robot.Localization
import frc.team5190.robot.Robot
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.auto.Trajectories
import openrio.powerup.MatchData
import org.apache.commons.math3.stat.regression.SimpleRegression

object Lidar {

    private val pwmData = DoubleArray(2)
    private var servo = Servo(Constants.kLidarServoId)
    private val regressionFunction = SimpleRegression()

    // All in inches
    private const val kMinScaleHeight = 48.0
    private const val kMaxScaleHeight = 72.0
    private const val kAllowedTolerance = 3.0

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

        Notifier(::run).startPeriodic(0.02)

    }

    private fun run() {
        Canifier.getPWMInput(CANifier.PWMChannel.PWMChannel0, pwmData)

        rawDistance = pwmData[0]
        scaleHeight = regressionFunction.predict(rawDistance)
        underScale = kMinScaleHeight - kAllowedTolerance < scaleHeight && scaleHeight < kMaxScaleHeight + kAllowedTolerance

        var angle = Localization.robotPosition.let {
            val scalePosition = Trajectories.kNearScaleFull.let { if (Autonomous.scaleSide == MatchData.OwnedSide.RIGHT) it.mirror else it }
            return@let Rotation2d((scalePosition.translation - it.translation), true).degrees + 180 + AHRS.correctedAngle.degrees
        }

        angle = ((angle + 90) % 360) - 90.0
        servo.angle = if (Robot.INSTANCE.isOperatorControl) 90.0 else angle
    }
}

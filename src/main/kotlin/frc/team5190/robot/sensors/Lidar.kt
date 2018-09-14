/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.sensors

import com.ctre.phoenix.CANifier
import edu.wpi.first.wpilibj.Servo
import frc.team5190.lib.mathematics.twodim.geometry.Pose2d
import frc.team5190.lib.mathematics.twodim.geometry.Rotation2d
import frc.team5190.lib.mathematics.twodim.geometry.Translation2d
import frc.team5190.lib.mathematics.units.Distance
import frc.team5190.lib.mathematics.units.Inches
import frc.team5190.lib.utils.Source
import frc.team5190.lib.utils.launchFrequency
import frc.team5190.lib.wrappers.FalconRobotBase
import frc.team5190.robot.Constants
import frc.team5190.robot.Localization
import frc.team5190.robot.auto.Autonomous
import frc.team5190.robot.auto.Trajectories
import openrio.powerup.MatchData
import org.apache.commons.math3.stat.regression.SimpleRegression

object Lidar : Source<Pair<Boolean, Distance>> {

    private val pwmData = DoubleArray(2)
    private var servo = Servo(Constants.kLidarServoId)
    private val regressionFunction = SimpleRegression()

    private val kNearScaleFull = Pose2d(Translation2d(23.95, 20.2), Rotation2d.fromDegrees(160.0))

    // All in inches
    private val kMinScaleHeight = Inches(48.0)
    private val kMaxScaleHeight = Inches(72.0)
    private val kAllowedTolerance = Inches(3.0)

    var underScale = false
        private set

    var scaleHeight: Distance = Inches(0.0)
        private set

    override val value
        get() = underScale to scaleHeight

    init {
        // X - Raw Sensor Units
        // Y - Height in Inches
        val data = arrayOf(
                1050.0 to 45.0,
                1500.0 to 55.0,
                1900.0 to 70.0
        )

        data.forEach { regressionFunction.addData(it.first, it.second) }

        launchFrequency { run() }
    }

    private fun run() {
        Canifier.getPWMInput(CANifier.PWMChannel.PWMChannel0, pwmData)

        val rawDistance = pwmData[0]
        scaleHeight = Inches(regressionFunction.predict(rawDistance))
        underScale = kMinScaleHeight - kAllowedTolerance < scaleHeight && scaleHeight < kMaxScaleHeight + kAllowedTolerance

        servo.angle = if (FalconRobotBase.INSTANCE.isOperatorControl) 90.0 else {
            val robotPosition = Localization.robotPosition
            val scalePosition = kNearScaleFull.let { if (Autonomous.Config.scaleSide.value == MatchData.OwnedSide.RIGHT) it.mirror else it }
            val angle = Rotation2d((scalePosition.translation - robotPosition.translation), true).degrees + 180 + AHRS.correctedAngle.degrees

            ((angle + 90) % 360) - 90.0
        }
    }
}

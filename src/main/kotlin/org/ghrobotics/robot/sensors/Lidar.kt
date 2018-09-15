/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.sensors

import com.ctre.phoenix.CANifier
import edu.wpi.first.wpilibj.Servo
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Rotation2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.units.Distance
import org.ghrobotics.lib.mathematics.units.Inches
import org.ghrobotics.lib.utils.Source
import org.ghrobotics.lib.utils.launchFrequency
import org.ghrobotics.lib.wrappers.FalconRobotBase
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.Localization
import org.ghrobotics.robot.auto.Autonomous
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

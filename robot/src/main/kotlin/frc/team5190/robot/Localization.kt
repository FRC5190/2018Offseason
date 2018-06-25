@file:Suppress("MemberVisibilityCanBePrivate")

package frc.team5190.robot

import edu.wpi.first.wpilibj.Notifier
import frc.team5190.lib.extensions.enforceBounds
import frc.team5190.lib.extensions.epsilonEquals
import frc.team5190.lib.math.Pose2D
import frc.team5190.lib.units.Distance
import frc.team5190.lib.units.NativeUnits
import frc.team5190.robot.drive.DriveSubsystem
import frc.team5190.robot.sensors.NavX
import jaci.pathfinder.Pathfinder
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import kotlin.math.cos
import kotlin.math.sin

object Localization {

    private const val ANGLE_FILTER = 0.9999

    private val loc = Object()

    var robotPosition = Pose2D(Vector2D.ZERO, 0.0)
        private set

    private var prevL: Distance = NativeUnits(0)
    private var prevR: Distance = NativeUnits(0)
    private var prevA = 0.0

    init {
        reset()
        Notifier(this::run).startPeriodic(0.05)
    }

    fun reset(pose: Pose2D = Pose2D(Vector2D.ZERO, 0.0)) {
        synchronized(loc) {
            robotPosition = pose
            prevL = DriveSubsystem.leftPosition
            prevR = DriveSubsystem.rightPosition
            prevA = Math.toRadians(NavX.correctedAngle)
        }
    }

    fun reset(vector2d: Vector2D) = reset(Pose2D(vector2d, 0.0))

    private fun run() {
        synchronized(loc) {
            val posL = DriveSubsystem.leftPosition
            val posR = DriveSubsystem.rightPosition

            // Run a basic filter to reduce uncessary noise
            val angA = Math.toRadians(Pathfinder.boundHalfDegrees(
                    (ANGLE_FILTER * NavX.correctedAngle) + ((1 - ANGLE_FILTER) * prevA)))


            val deltaL = posL - prevL
            val deltaR = posR - prevR
            val deltaA = (angA - prevA).enforceBounds()

            val distance = ((deltaL + deltaR) / 2.0).FT.value

            val sinDeltaA = sin(deltaA)
            val cosDeltaA = cos(deltaA)

            val s: Double
            val c: Double

            if (deltaA epsilonEquals 0.0) {
                s = 1.0 - 1.0 / 6.0 * deltaA * deltaA
                c = .5 * deltaA
            } else {
                s = sinDeltaA / deltaA
                c = (1.0 - cosDeltaA) / deltaA
            }

            val x = distance * s
            val y = distance * c

            val prevACos = cos(prevA)
            val prevASin = sin(prevA)

            val vector = Vector2D(
                    x * prevACos - y * prevASin,
                    x * prevASin + y * prevACos)

            robotPosition = Pose2D(robotPosition.positionVector.add(vector), angA)

            prevL = posL
            prevR = posR
            prevA = angA
        }
    }
}

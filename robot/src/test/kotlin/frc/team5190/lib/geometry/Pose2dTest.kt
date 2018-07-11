/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.geometry

import frc.team5190.lib.motion.kinematics.FrameOfReference
import org.junit.Test

class Pose2dTest {
    @Test
    fun testCoordinateTransformation() {
        val originalPose = Pose2d(Translation2d(0.0, 0.0), Rotation2d.fromDegrees(45.0))
        val newFOR = FrameOfReference(Translation2d(1.0, 1.0), Rotation2d.fromDegrees(45.0))

        val newCoords = originalPose.convertToFOR(newFOR)
        println("X: ${newCoords.translation.x}, Y: ${newCoords.translation.y}, Theta: ${newCoords.rotation.degrees}")
    }
}
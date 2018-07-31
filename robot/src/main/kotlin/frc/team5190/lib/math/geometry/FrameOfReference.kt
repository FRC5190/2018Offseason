/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.math.geometry

class FrameOfReference (val fieldRelativeOrigin: Pose2d) {
    companion object {
        val kField = FrameOfReference(Pose2d())
    }
}
/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.motion.kinematics

import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.geometry.Translation2d

class FrameOfReference(val originRelativeToField: Translation2d,
                       val orientationRelativeToField: Rotation2d) {
    companion object {
        val kField = FrameOfReference(Translation2d(), Rotation2d())
    }
}

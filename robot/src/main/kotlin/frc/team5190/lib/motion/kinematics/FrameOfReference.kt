/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */

package frc.team5190.lib.motion.kinematics

import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.geometry.Translation2d

class FrameOfReference(val originRelativeToField: Translation2d,
                       val orientationRelativeToField: Rotation2d) {
    companion object {
        val FIELD = FrameOfReference(Translation2d(), Rotation2d())
    }
}

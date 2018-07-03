package frc.team5190.lib.kinematics

class FramesOfReference(val originRelativeToField: Translation2d,
                        val orientationRelativeToField: Rotation2d) {
    companion object {
        val FIELD = FramesOfReference(Translation2d(), Rotation2d())
    }
}

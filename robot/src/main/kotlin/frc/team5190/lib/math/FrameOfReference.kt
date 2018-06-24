package frc.team5190.lib.math

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

class FrameOfReference(val originRelativeToField: Vector2D, val orientationRelativeToField: Double) {
    companion object {
        val FIELD = FrameOfReference(Vector2D(0.0, 0.0), 0.0)
    }
}

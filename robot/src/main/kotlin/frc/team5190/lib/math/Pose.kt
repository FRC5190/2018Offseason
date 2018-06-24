package frc.team5190.lib.math

import frc.team5190.lib.Matrix
import frc.team5190.lib.times
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import kotlin.math.cos
import kotlin.math.sin

data class Pose2D(val positionVector: Vector2D,
                  val angle: Double,
                  val frameOfReference: FrameOfReference = FrameOfReference.FIELD) {


    fun convertTo(other: FrameOfReference): Pose2D {
        val theta = frameOfReference.orientationRelativeToField - other.orientationRelativeToField

        val rotationMatrixData = arrayOf(
                doubleArrayOf(cos(theta), -sin(theta), frameOfReference.originRelativeToField.x - other.originRelativeToField.x),
                doubleArrayOf(sin(theta), cos(theta), frameOfReference.originRelativeToField.y - other.originRelativeToField.y),
                doubleArrayOf(0.0, 0.0, 1.0)
        )

        val positionMatrixData = arrayOf(
                doubleArrayOf(this.positionVector.x),
                doubleArrayOf(this.positionVector.y),
                doubleArrayOf(1.0))

        val rotationMatrix = Matrix(rotationMatrixData)
        val positionMatrix = Matrix(positionMatrixData)

        val result = rotationMatrix * positionMatrix
        return Pose2D(Vector2D(result.getEntry(0, 0), result.getEntry(1, 0)), angle + theta)
    }
}



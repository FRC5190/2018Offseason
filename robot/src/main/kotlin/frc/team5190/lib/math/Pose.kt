package frc.team5190.lib.math

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D


data class Pose3D(var vector: Vector3D, var yaw: Double, var pitch: Double) {

    constructor(vector: Vector2D) : this(Vector3D(vector.x, vector.y, 0.0), 0.0, 0.0)

    val pose2d = Pose2D(Vector2D(vector.x, vector.y), yaw)
}

data class Pose2D(var vector: Vector2D, var yaw: Double)
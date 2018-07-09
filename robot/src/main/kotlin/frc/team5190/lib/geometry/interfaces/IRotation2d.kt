package frc.team5190.lib.geometry.interfaces

import frc.team5190.lib.geometry.Rotation2d

interface IRotation2d<S> : State<S> {
    val rotation: Rotation2d
}

/*
 * FRC Team 5190
 * Green Hope Falcons
 */

/*
 * Some implementations and algorithms borrowed from:
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 */


package frc.team5190.lib.geometry.interfaces

import frc.team5190.lib.geometry.Rotation2d

interface IRotation2d<S> : State<S> {
    val rotation: Rotation2d
}

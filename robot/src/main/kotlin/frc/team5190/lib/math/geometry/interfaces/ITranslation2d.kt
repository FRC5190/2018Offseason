/*
 * FRC Team 5190
 * Green Hope Falcons
 */

/*
 * Some implementations and algorithms borrowed from:
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 */


package frc.team5190.lib.math.geometry.interfaces

import frc.team5190.lib.math.geometry.Translation2d

interface ITranslation2d<S> : State<S> {
    val translation: Translation2d
}

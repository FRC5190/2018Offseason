/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */

package frc.team5190.lib.geometry.interfaces

import frc.team5190.lib.geometry.Translation2d


interface ITranslation2d<S> : State<S> {
    val translation: Translation2d
}
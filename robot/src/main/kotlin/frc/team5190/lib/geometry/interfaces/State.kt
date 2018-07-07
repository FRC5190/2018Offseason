/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */

package frc.team5190.lib.geometry.interfaces

import frc.team5190.lib.types.Interpolable


interface State<S> : Interpolable<S> {
    fun distance (other: S): Double
}


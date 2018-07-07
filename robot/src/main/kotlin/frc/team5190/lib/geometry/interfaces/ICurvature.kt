/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */

package frc.team5190.lib.geometry.interfaces


interface ICurvature<S> : State<S> {
    val curvature: Double
    val dkds: Double
}

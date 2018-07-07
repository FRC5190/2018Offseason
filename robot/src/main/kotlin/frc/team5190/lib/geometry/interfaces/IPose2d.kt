/*
 * Original Work by
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 *
 * Rewritten and Modified in Kotlin by Team 5190
 */

package frc.team5190.lib.geometry.interfaces

import frc.team5190.lib.geometry.Pose2d


interface IPose2d<S> : IRotation2d<S>, ITranslation2d<S> {
    val pose: Pose2d
    fun transformBy(transform: Pose2d): S
    fun mirror(): S
}
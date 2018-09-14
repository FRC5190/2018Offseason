package frc.team5190.robot.auto.routines

import frc.team5190.lib.commands.*
import frc.team5190.lib.mathematics.twodim.geometry.Translation2d
import frc.team5190.lib.utils.Source
import frc.team5190.lib.utils.map
import frc.team5190.lib.utils.mergeSource
import frc.team5190.robot.auto.StartingPositions
import frc.team5190.robot.auto.Trajectories
import frc.team5190.robot.subsytems.SubsystemPreset
import frc.team5190.robot.subsytems.arm.ArmSubsystem
import frc.team5190.robot.subsytems.arm.ClosedLoopArmCommand
import frc.team5190.robot.subsytems.drive.FollowTrajectoryCommand
import frc.team5190.robot.subsytems.elevator.ClosedLoopElevatorCommand
import frc.team5190.robot.subsytems.elevator.ElevatorSubsystem
import frc.team5190.robot.subsytems.intake.IntakeCommand
import frc.team5190.robot.subsytems.intake.IntakeSubsystem
import openrio.powerup.MatchData
import java.util.concurrent.TimeUnit

class RoutineScaleFromSide(startingPosition: Source<StartingPositions>,
                           private val scaleSide: Source<MatchData.OwnedSide>) : AutoRoutine(startingPosition) {

    override fun createRoutine(): Command {
        val cross = mergeSource(startingPosition, scaleSide) { one, two -> !one.name.first().equals(two.name.first(), true) }
        val shouldMirrorPath = scaleSide.withEquals(MatchData.OwnedSide.RIGHT)

        val drop1stCube = FollowTrajectoryCommand(
                trajectory = cross.map(Trajectories.leftStartToFarScale, Trajectories.leftStartToNearScale),
                pathMirrored = startingPosition.withEquals(StartingPositions.RIGHT))

        val pickup2ndCube = FollowTrajectoryCommand(Trajectories.scaleToCube1, shouldMirrorPath)
        val drop2ndCube = FollowTrajectoryCommand(Trajectories.cube1ToScale, shouldMirrorPath)
        val pickup3rdCube = FollowTrajectoryCommand(Trajectories.scaleToCube2, shouldMirrorPath)
        val drop3rdCube = FollowTrajectoryCommand(Trajectories.cube2ToScale, shouldMirrorPath)
        val pickup4thCube = FollowTrajectoryCommand(Trajectories.scaleToCube3, shouldMirrorPath)

        val after2ndCube = DelayCommand(250, TimeUnit.MILLISECONDS)

        val elevatorUp = drop1stCube.addMarkerAt(Translation2d(11.0, 23.1))
        val shoot1stCube = drop1stCube.addMarkerAt(shouldMirrorPath.map(Translation2d(22.3, 20.6), Translation2d(19.0, 8.0)))
        val shoot2ndCube = drop2ndCube.addMarkerAt(Translation2d(22.5, 19.9))
        val shoot3rdCube = drop3rdCube.addMarkerAt(Translation2d(22.5, 19.9))

        return parallel {
        }
    }
}
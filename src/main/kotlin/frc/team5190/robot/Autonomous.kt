package frc.team5190.robot

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.team5190.lib.commandGroup
import frc.team5190.lib.util.Pathreader
import frc.team5190.lib.util.StateCommand
import frc.team5190.robot.arm.ArmPosition
import frc.team5190.robot.arm.AutoArmCommand
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.robot.elevator.ElevatorPreset
import frc.team5190.robot.elevator.ElevatorPresetCommand
import frc.team5190.robot.intake.IntakeCommand
import frc.team5190.robot.intake.IntakeDirection
import frc.team5190.robot.sensors.Pigeon
import kotlinx.coroutines.experimental.async
import openrio.powerup.MatchData
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

object Autonomous {

    // Switch side and scale side variables
    var switchSide = MatchData.OwnedSide.UNKNOWN
        private set
        @Synchronized get

    var scaleSide = MatchData.OwnedSide.UNKNOWN
        private set
        @Synchronized get

    // Starting position
    private var startingPosition = StartingPosition.CENTER

    // Contains folder IN which paths are located
    private var folder = ""

    // Is FMS Data valid
    private val fmsDataValid
        get() = switchSide != MatchData.OwnedSide.UNKNOWN && scaleSide != MatchData.OwnedSide.UNKNOWN

    init {
        val startingPositionChooser = SendableChooser<StartingPosition>()
        StartingPosition.values().forEach { startingPositionChooser.addObject(it.name.toLowerCase().capitalize(), it) }
        SmartDashboard.putData("Starting Position", startingPositionChooser)

        // Poll for FMS Data
        async {
            while (!(Robot.INSTANCE.isAutonomous && Robot.INSTANCE.isEnabled && fmsDataValid && Pathreader.pathsGenerated)) {
                switchSide = MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR)
                scaleSide = MatchData.getOwnedSide(MatchData.GameFeature.SCALE)
                startingPosition = startingPositionChooser.selected

                Robot.INSTANCE.isAutoReady = startingPositionChooser.selected != null && fmsDataValid && Pathreader.pathsGenerated
            }

            folder = if (startingPosition.name.first().toUpperCase() == scaleSide.name.first().toUpperCase()) "LS-LL" else "LS-RR"
            start()
        }
    }

    private fun start() {
        Pigeon.reset()

        val cube1 = FollowPathCommand(
                folder = folder,
                file = "1st Cube",
                resetRobotPosition = true,
                robotReversed = true,
                pathMirrored = startingPosition == StartingPosition.RIGHT).apply {

            addMarkerAt(Vector2D(14.0, 21.0), "Elevator Up")
            addMarkerAt(Vector2D(20.5, 23.0), "Shoot")

        }

        commandGroup {
            addSequential(commandGroup {
                addParallel(cube1)
                addParallel(commandGroup {
                    addSequential(AutoArmCommand(ArmPosition.UP))

                    addSequential(StateCommand(cube1.hasPassedMarker("Elevator Up")))
                    addSequential(ElevatorPresetCommand(ElevatorPreset.BEHIND))

                    addSequential(StateCommand(cube1.hasPassedMarker("Shoot")))
                    addSequential(IntakeCommand(IntakeDirection.OUT, speed = 1.0, timeout = 0.50))
                })
            })
        }.start()
    }

}

enum class StartingPosition {
    LEFT, CENTER, RIGHT
}
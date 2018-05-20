package frc.team5190.robot

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.team5190.lib.commandGroup
import frc.team5190.lib.util.Pathreader
import frc.team5190.robot.arm.ArmPosition
import frc.team5190.robot.arm.AutoArmCommand
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.robot.elevator.AutoElevatorCommand
import frc.team5190.robot.elevator.ElevatorPosition
import frc.team5190.robot.elevator.ElevatorPreset
import frc.team5190.robot.elevator.ElevatorPresetCommand
import frc.team5190.robot.intake.IntakeCommand
import frc.team5190.robot.intake.IntakeDirection
import frc.team5190.robot.sensors.Pigeon
import kotlinx.coroutines.experimental.async
import openrio.powerup.MatchData

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
        when (folder) {
            "LS-LL" -> commandGroup {
                addSequential(commandGroup {
                    addParallel(FollowPathCommand(folder = "LS-LL",
                            file = "1st Cube",
                            resetRobotPosition = true,
                            robotReversed = true,
                            pathMirrored = startingPosition == StartingPosition.RIGHT))
                    addParallel(commandGroup {
                        addParallel(AutoElevatorCommand(ElevatorPosition.SWITCH))
                        addParallel(AutoArmCommand(ArmPosition.UP))
                    })
                })
                addSequential(ElevatorPresetCommand(ElevatorPreset.BEHIND))
                addSequential(IntakeCommand(IntakeDirection.OUT, timeout = 0.75, speed = 0.75))
            }
            "LS-RR" -> commandGroup {
                addSequential(commandGroup {
                    addParallel(FollowPathCommand(folder = "LS-RR",
                            file = "1st Cube",
                            resetRobotPosition = true,
                            robotReversed = true,
                            pathMirrored = startingPosition == StartingPosition.RIGHT))
                    addParallel(commandGroup {
                        addParallel(AutoElevatorCommand(ElevatorPosition.SWITCH))
                        addParallel(AutoArmCommand(ArmPosition.UP))
                    })
                })
                addSequential(ElevatorPresetCommand(ElevatorPreset.BEHIND))
                addSequential(IntakeCommand(IntakeDirection.OUT, timeout = 0.75, speed = 0.75))
            }
            else -> null
        }?.start()
    }

}

enum class StartingPosition {
    LEFT, CENTER, RIGHT
}
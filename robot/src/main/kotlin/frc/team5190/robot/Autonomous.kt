package frc.team5190.robot

import edu.wpi.first.wpilibj.command.CommandGroup
import frc.team5190.lib.commandGroup
import frc.team5190.lib.util.Pathreader
import frc.team5190.lib.util.StateCommand
import frc.team5190.robot.arm.ArmPosition
import frc.team5190.robot.arm.AutoArmCommand
import frc.team5190.robot.drive.FollowPathCommand
import frc.team5190.robot.drive.Marker
import frc.team5190.robot.elevator.ElevatorPreset
import frc.team5190.robot.elevator.ElevatorPresetCommand
import frc.team5190.robot.intake.IntakeCommand
import frc.team5190.robot.intake.IntakeDirection
import frc.team5190.robot.sensors.Pigeon
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import openrio.powerup.MatchData
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

object Autonomous {

    // Switch side and scale side variables
    private var switchSide = MatchData.OwnedSide.UNKNOWN
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
        // Poll for FMS Data
        async {

            var autoCommand = commandGroup { }

            while (!(Robot.INSTANCE.isAutonomous && Robot.INSTANCE.isEnabled && fmsDataValid && Pathreader.pathsGenerated)) {

                if (StartingPosition.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase()) != startingPosition ||
                        MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR) != switchSide ||
                        MatchData.getOwnedSide(MatchData.GameFeature.SCALE) != scaleSide) {

                    switchSide = MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR)
                    scaleSide = MatchData.getOwnedSide(MatchData.GameFeature.SCALE)
                    startingPosition = StartingPosition.valueOf(NetworkInterface.startingPosition.getString("Left").toUpperCase())

                    autoCommand = getAutoCommand()
                    Robot.INSTANCE.isAutoReady = false

                    delay(100)
                }
                Robot.INSTANCE.isAutoReady = fmsDataValid && Pathreader.pathsGenerated
            }

            folder = if (startingPosition.name.first().toUpperCase() == scaleSide.name.first().toUpperCase()) "LS-LL" else "LS-RR"
            autoCommand.start()
        }
    }

    private fun getAutoCommand(): CommandGroup {
        Pigeon.reset()
        Pigeon.angleOffset = 180.00

        NetworkInterface.ntInstance.getEntry("Reset").setBoolean(true)

        /*
        run {
            val elevatorUp: Marker
            val shootCube1: Marker
            val shootCube2: Marker
            val shootCube3: Marker

            val cube1 = FollowPathCommand(
                    folder = folder,
                    file = "1st Cube",
                    resetRobotPosition = true,
                    robotReversed = true,
                    pathMirrored = startingPosition == StartingPosition.RIGHT)

            val rightScale = scaleSide == MatchData.OwnedSide.RIGHT

            val pickupCube2 = FollowPathCommand(folder = "LS-LL", file = "2nd Cube", pathMirrored = rightScale)
            val pickupCube3 = FollowPathCommand(folder = "LS-LL", file = "3rd Cube", pathMirrored = rightScale)

            val dropCube2 = FollowPathCommand(folder = "LS-LL", file = "2nd Cube", robotReversed = true, pathReversed = true, pathMirrored = rightScale)
            val dropCube3 = FollowPathCommand(folder = "LS-LL", file = "3rd Cube", robotReversed = true, pathReversed = true, pathMirrored = rightScale)


            if (folder == "LS-LL") {
                elevatorUp = cube1.addMarkerAt(Vector2D(14.0, 23.5))
                shootCube1 = cube1.addMarkerAt(Vector2D(22.9, 20.0))
            } else {
                elevatorUp = cube1.addMarkerAt(Vector2D(20.5, 7.0))
                shootCube1 = cube1.addMarkerAt(Vector2D(22.9, 7.0))
            }

            shootCube2 = dropCube2.addMarkerAt(Vector2D(22.9, 20.0))
            shootCube3 = dropCube3.addMarkerAt(Vector2D(22.9, 20.0))

            return commandGroup {
                addSequential(commandGroup {
                    addParallel(cube1)
                    addParallel(commandGroup {
                        addSequential(AutoArmCommand(ArmPosition.UP))

                        addSequential(StateCommand { cube1.hasPassedMarker(elevatorUp) })
                        addSequential(ElevatorPresetCommand(ElevatorPreset.BEHIND))

                        addSequential(StateCommand { cube1.hasPassedMarker(shootCube1) })
                        addSequential(IntakeCommand(IntakeDirection.OUT, speed = 1.0, timeout = 0.50))
                    })
                })

                addSequential(commandGroup {
                    addParallel(pickupCube2)
                    addParallel(ElevatorPresetCommand(ElevatorPreset.INTAKE))
                    addParallel(IntakeCommand(IntakeDirection.IN, speed = 1.0) { pickupCube2.isCompleted })
                })

                addSequential(commandGroup {
                    addParallel(dropCube2)
                    addParallel(commandGroup {
                        addSequential(ElevatorPresetCommand(ElevatorPreset.BEHIND))

                        addSequential(StateCommand { dropCube2.hasPassedMarker(shootCube2) })
                        addSequential(IntakeCommand(IntakeDirection.OUT, speed = 1.0, timeout = 0.50))
                    })
                })

                addSequential(commandGroup {
                    addParallel(pickupCube3)
                    addParallel(ElevatorPresetCommand(ElevatorPreset.INTAKE))
                    addParallel(IntakeCommand(IntakeDirection.IN, speed = 1.0) { pickupCube3.isCompleted })
                })

                addSequential(commandGroup {
                    addParallel(dropCube3)
                    addParallel(commandGroup {
                        addSequential(ElevatorPresetCommand(ElevatorPreset.BEHIND))

                        addSequential(StateCommand { dropCube3.hasPassedMarker(shootCube3) })
                        addSequential(IntakeCommand(IntakeDirection.OUT, speed = 1.0, timeout = 0.50))
                    })
                })
            }
        }
        */

        return commandGroup {
            addSequential(FollowPathCommand(folder = "LS-LL", file = "1st Cube", resetRobotPosition = true))
        }
    }
}

enum class StartingPosition {
    LEFT, CENTER, RIGHT
}
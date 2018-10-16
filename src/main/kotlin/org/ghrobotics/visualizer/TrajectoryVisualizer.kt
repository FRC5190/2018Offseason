package org.ghrobotics.visualizer

/*
import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedTrajectory
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.mathematics.units.derivedunits.feetPerSecond
import org.ghrobotics.lib.mathematics.units.derivedunits.velocity
import org.ghrobotics.lib.mathematics.units.meter
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.auto.Trajectories
import tornadofx.*

class TrajectoryVisualizer : App(MainView::class) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<TrajectoryVisualizer>(args)
        }
    }
}

class MainView : View() {
    override val root = vbox { }

    init {
        title = "Trajectory Visualizer"
        with(root) {
            tabpane {
                tab("Position") {
                    add(PositionChart)
                    isClosable = false
                }
                tab("Velocity") {
                    add(VelocityChart)
                    isClosable = false
                }
            }
            hbox {
                style {
                    paddingAll = 10
                    backgroundColor = MultiValue(arrayOf(Color.LIGHTGRAY))
                }
                combobox<Trajectories.Container> {
                    alignment = Pos.CENTER
                    items = Trajectories.trajectories.observable()
                    selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                        PositionChart.updateChart(newValue.trajectory)
                        VelocityChart.updateChart(newValue.trajectory)
                    }
                }
            }
        }
    }
}

object PositionChart : LineChart<Number, Number>(NumberAxis(0.0, 54.0, 1.0), NumberAxis(0.0, 27.0, 1.0)) {
    init {
        style {
            backgroundColor = MultiValue(arrayOf<Paint>(Color.LIGHTGRAY))
        }
        lookup(".chart-plot-background").style +=
                "-fx-background-image: url(\"field.png\");" +
                "-fx-background-size: stretch;" +
                "-fx-background-position: top right;" +
                "-fx-background-repeat: no-repeat;"

        setMinSize(54 * 25.0, 27 * 25.0)

        axisSortingPolicy = LineChart.SortingPolicy.NONE
        isLegendVisible = false
        animated = false
    }

    fun updateChart(trajectory: TimedTrajectory<Pose2dWithCurvature>) {
        data.clear()

        val seriesXY = XYChart.Series<Number, Number>()
        val seriesRobotStart = XYChart.Series<Number, Number>()
        val seriesRobotEnd = XYChart.Series<Number, Number>()

        with(seriesXY) {
            val iterator = trajectory.iterator()
            while (!iterator.isDone) {
                val point = iterator.advance(0.02.second)
                data(point.state.state.pose.translation.x.feet.asDouble, point.state.state.pose.translation.y.feet.asDouble)
            }
            this@PositionChart.data.add(this)
        }

        with(seriesRobotStart) {
            getRobotBoundingBox(trajectory.firstState.state.pose).forEach {
                data(it.translation.x.feet.asDouble, it.translation.y.feet.asDouble)
            }
            this@PositionChart.data.add(this)
        }

        with(seriesRobotEnd) {
            getRobotBoundingBox(trajectory.lastState.state.pose).forEach {
                data(it.translation.x.feet.asDouble, it.translation.y.feet.asDouble)
            }
            this@PositionChart.data.add(this)
        }
    }

    private fun getRobotBoundingBox(center: Pose2d): Array<Pose2d> {
        val tl = center.transformBy(Pose2d(Translation2d(-Constants.kRobotLength / 2, Constants.kRobotWidth / 2), 0.degree))
        val tr = center.transformBy(Pose2d(Translation2d(Constants.kRobotLength / 2, Constants.kRobotWidth / 2), 0.degree))
        val bl = center.transformBy(Pose2d(Translation2d(-Constants.kRobotLength / 2, -Constants.kRobotWidth / 2), 0.degree))
        val br = center.transformBy(Pose2d(Translation2d(Constants.kRobotLength / 2, -Constants.kRobotWidth / 2), 0.degree))

        val average = (tr.translation + br.translation) * 0.5
        val middle = Pose2d(average, center.rotation).transformBy(Pose2d(-Constants.kFrontToIntake.translation, Constants.kFrontToIntake.rotation))

        return arrayOf(tl, tr, middle, br, bl, tl)
    }
}

object VelocityChart : LineChart<Number, Number>(NumberAxis(), NumberAxis()) {
    init {
        style {
            backgroundColor = MultiValue(arrayOf<Paint>(Color.LIGHTGRAY))
        }

        setMinSize(54 * 25.0, 27 * 25.0)

        axisSortingPolicy = LineChart.SortingPolicy.NONE
        isLegendVisible = false
        createSymbols = false
        animated = false
    }

    fun updateChart(trajectory: TimedTrajectory<Pose2dWithCurvature>) {
        data.clear()

        val seriesVelocity = XYChart.Series<Number, Number>()

        with(seriesVelocity) {
            val iterator = trajectory.iterator()
            while (!iterator.isDone) {
                val point = iterator.advance(0.02.second)
                data(point.state.t, point.state.velocity.meter.velocity.feetPerSecond.asDouble)
            }
            this@VelocityChart.data.add(this)
        }
    }
}
*/

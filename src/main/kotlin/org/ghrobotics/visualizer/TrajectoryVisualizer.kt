package org.ghrobotics.visualizer

/*
import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.TimedState
import org.ghrobotics.lib.mathematics.twodim.trajectory.Trajectory
import org.ghrobotics.lib.mathematics.twodim.trajectory.TrajectoryIterator
import org.ghrobotics.lib.mathematics.twodim.trajectory.view.TimedView
import org.ghrobotics.robot.Constants
import org.ghrobotics.robot.auto.Trajectories
import tornadofx.*
import kotlin.math.absoluteValue

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

    fun updateChart(trajectory: Trajectory<TimedState<Pose2dWithCurvature>>) {
        data.clear()

        val seriesXY = XYChart.Series<Number, Number>()
        val seriesRobotStart = XYChart.Series<Number, Number>()
        val seriesRobotEnd = XYChart.Series<Number, Number>()

        with(seriesXY) {
            val iterator = TrajectoryIterator(TimedView(trajectory))
            while (!iterator.isDone) {
                val point = iterator.advance(0.02)
                data(point.state.state.translation.x, point.state.state.translation.y)
            }
            this@PositionChart.data.add(this)
        }

        with(seriesRobotStart) {
            getRobotBoundingBox(trajectory.firstState.state.pose).forEach {
                data(it.translation.x, it.translation.y)
            }
            this@PositionChart.data.add(this)
        }

        with(seriesRobotEnd) {
            getRobotBoundingBox(trajectory.lastState.state.pose).forEach {
                data(it.translation.x, it.translation.y)
            }
            this@PositionChart.data.add(this)
        }
    }

    private fun getRobotBoundingBox(center: Pose2d): Array<Pose2d> {
        val tl = center.transformBy(Pose2d.fromTranslation(Translation2d(-Constants.kRobotLength / 2, Constants.kRobotWidth / 2)))
        val tr = center.transformBy(Pose2d.fromTranslation(Translation2d(Constants.kRobotLength / 2, Constants.kRobotWidth / 2)))
        val bl = center.transformBy(Pose2d.fromTranslation(Translation2d(-Constants.kRobotLength / 2, -Constants.kRobotWidth / 2)))
        val br = center.transformBy(Pose2d.fromTranslation(Translation2d(Constants.kRobotLength / 2, -Constants.kRobotWidth / 2)))

        val average = (tr.translation + br.translation).scale(0.5)
        val middle = Pose2d(average, center.rotation).transformBy(Pose2d(Constants.kFrontToIntake.translation.inverse, Constants.kFrontToIntake.rotation))

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

    fun updateChart(trajectory: Trajectory<TimedState<Pose2dWithCurvature>>) {
        data.clear()

        val seriesVelocity = XYChart.Series<Number, Number>()

        with(seriesVelocity) {
            val iterator = TrajectoryIterator(TimedView(trajectory))
            while (!iterator.isDone) {
                val point = iterator.advance(0.02)
                data(point.state.t, point.state.velocity.absoluteValue)
            }
            this@VelocityChart.data.add(this)
        }
    }
}
*/
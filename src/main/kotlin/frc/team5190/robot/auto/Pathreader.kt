package frc.team5190.robot.auto

import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import jaci.pathfinder.Waypoint
import jaci.pathfinder.modifiers.TankModifier
import kotlinx.coroutines.experimental.launch
import org.jsoup.Jsoup
import java.io.File

object Pathreader {

    private lateinit var allPaths: Map<String, ArrayList<Trajectory>>

    var pathsGenerated = false
        private set

    init {
        // Launch coroutine to generate paths so it doesn't lag robot.
        launch {
            allPaths = File("/home/lvuser/paths/Raw XMLs").listFiles().filter { it.isDirectory }.map { folder ->
                folder.listFiles().filter { it.isFile }.map { file ->
                    "$folder/${file.nameWithoutExtension}" to getPathCollection(folder.name, file.nameWithoutExtension)
                }
            }.flatten().toMap()

            pathsGenerated = true
        }
    }

    private fun getPathCollection(folder: String, file: String): ArrayList<Trajectory> {
        val xml = File("/home/lvuser/paths/Raw XMLs/$folder/$file.xml")

        val leftFilePath = "/home/lvuser/paths/$folder/${xml.nameWithoutExtension}-${xml.hashCode()} Left Detailed.csv"
        val rightFilePath = "/home/lvuser/paths/$folder/${xml.nameWithoutExtension}-${xml.hashCode()} Right Detailed.csv"
        val sourceFilePath = "/home/lvuser/paths/$folder/${xml.nameWithoutExtension}-${xml.hashCode()} Source Detailed.csv"

        val leftFile = File(leftFilePath)
        val rightFile = File(rightFilePath)
        val sourceFile = File(sourceFilePath)

        var leftTrajectory = Pathfinder.readFromCSV(leftFile)
        var rightTrajectory = Pathfinder.readFromCSV(rightFile)
        var sourceTrajectory = Pathfinder.readFromCSV(sourceFile)

        if (!leftFile.isFile || !rightFile.isFile || !sourceFile.isFile) {
            launch {
                val doc = Jsoup.parse(javaClass.classLoader.getResourceAsStream("XML Files/$folder/${xml.name}}").use {
                    it.bufferedReader().readText()
                })

                val trajectoryElement = doc.getElementsByTag("Trajectory").first()

                val timeStep = trajectoryElement.attr("dt").toDouble()
                val velocity = trajectoryElement.attr("velocity").toDouble()
                val acceleration = trajectoryElement.attr("acceleration").toDouble()
                val jerk = trajectoryElement.attr("jerk").toDouble()
                val wheelBaseW = trajectoryElement.attr("wheelBaseW").toDouble()

                val fitMethod = Trajectory.FitMethod.valueOf(trajectoryElement.attr("fitMethod"))

                val waypoints = trajectoryElement.getElementsByTag("Waypoint").map { waypointElement ->
                    val xText = waypointElement.getElementsByTag("X").first().text().toDouble()
                    val yText = waypointElement.getElementsByTag("Y").first().text().toDouble()
                    val angleText = waypointElement.getElementsByTag("Angle").first().text().toDouble()

                    Waypoint(xText, yText, angleText)
                }.toTypedArray()

                val config = Trajectory.Config(fitMethod, Trajectory.Config.SAMPLES_HIGH, timeStep, velocity, acceleration, jerk)
                val trajectory = Pathfinder.generate(waypoints, config)
                val modifier = TankModifier(trajectory)
                modifier.modify(wheelBaseW)

                leftTrajectory = modifier.leftTrajectory
                rightTrajectory = modifier.rightTrajectory
                sourceTrajectory = modifier.sourceTrajectory

                Pathfinder.writeToCSV(sourceFile, sourceTrajectory)
                Pathfinder.writeToCSV(leftFile, leftTrajectory)
                Pathfinder.writeToCSV(rightFile, rightTrajectory)
            }
        }
        return arrayListOf(leftTrajectory, rightTrajectory, sourceTrajectory)
    }


    fun getPaths(folder: String, file: String): ArrayList<Trajectory> {
        return allPaths["$folder/$file"]!!
    }
}
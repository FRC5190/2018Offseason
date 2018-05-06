package frc.team5190.robot.auto

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import jaci.pathfinder.Waypoint
import jaci.pathfinder.modifiers.TankModifier
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileReader

object Pathreader {

    private lateinit var allPaths: Map<String, ArrayList<Trajectory>>

    var pathsGenerated = false
        private set

    init {
        // Launch coroutine to generate paths so it doesn't lag robot.
        launch {
            allPaths = File("/home/lvuser/paths/Raw JSONs").listFiles().filter { it.isDirectory }.map { folder ->
                folder.listFiles().filter { it.isFile }.map { file ->
                    "$folder/${file.nameWithoutExtension}" to getPathCollection(folder.name, file.nameWithoutExtension).await()
                }
            }.flatten().toMap()

            pathsGenerated = true
        }
    }

    private fun getPathCollection(folder: String, file: String) = async {
        val json = File("/home/lvuser/paths/Raw XMLs/$folder/$file.json")

        val leftFilePath = "/home/lvuser/paths/$folder/${json.nameWithoutExtension}-${json.hashCode()} Left Detailed.csv"
        val rightFilePath = "/home/lvuser/paths/$folder/${json.nameWithoutExtension}-${json.hashCode()} Right Detailed.csv"
        val sourceFilePath = "/home/lvuser/paths/$folder/${json.nameWithoutExtension}-${json.hashCode()} Source Detailed.csv"

        val leftFile = File(leftFilePath)
        val rightFile = File(rightFilePath)
        val sourceFile = File(sourceFilePath)

        var leftTrajectory = Pathfinder.readFromCSV(leftFile)
        var rightTrajectory = Pathfinder.readFromCSV(rightFile)
        var sourceTrajectory = Pathfinder.readFromCSV(sourceFile)

        if (!leftFile.isFile || !rightFile.isFile || !sourceFile.isFile) {
            val generationInfo = Gson().fromJson<PathGeneratorInfo>(FileReader(json))
            val config = Trajectory.Config(generationInfo.fitMethod, generationInfo.sampleRate, generationInfo.dt, generationInfo.vmax, generationInfo.amax, generationInfo.jmax)

            val waypoints = generationInfo.waypoints.map {
                Waypoint(it.first, it.second, it.third)
            }.toTypedArray()

            val trajectory = Pathfinder.generate(waypoints, config)
            val modifier = TankModifier(trajectory)
            modifier.modify(generationInfo.wheelbasewidth)

            leftTrajectory = modifier.leftTrajectory
            rightTrajectory = modifier.rightTrajectory
            sourceTrajectory = modifier.sourceTrajectory

            Pathfinder.writeToCSV(sourceFile, sourceTrajectory)
            Pathfinder.writeToCSV(leftFile, leftTrajectory)
            Pathfinder.writeToCSV(rightFile, rightTrajectory)

        }
        return@async arrayListOf(leftTrajectory, rightTrajectory, sourceTrajectory)
    }


    fun getPaths(folder: String, file: String): ArrayList<Trajectory> {
        return allPaths["$folder/$file"]!!
    }
}

data class PathGeneratorInfo(val dt: Double, val vmax: Double, val amax: Double, val jmax: Double, val wheelbasewidth: Double, val waypoints: ArrayList<Triple<Double, Double, Double>>, val fitMethod: Trajectory.FitMethod, val sampleRate: Int)
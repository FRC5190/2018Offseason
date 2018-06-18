package frc.team5190.lib.util

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import frc.team5190.lib.md5
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import jaci.pathfinder.Waypoint
import java.io.File
import java.io.FileReader

object Pathreader {

    private val allPaths = File("/home/lvuser/paths/").listFiles().filter { it.isDirectory }.map { folder ->
        folder.listFiles().filter { it.isFile }.map { file ->
            Pair("${folder.name}/${file.nameWithoutExtension}", Pathfinder.readFromCSV(file))
        }
    }.flatten().toMap()

    var pathsGenerated = true
        private set

    fun getPathCollection(filePath: String): Trajectory {

        println("Generating Paths")

        val json = File(filePath)
        val sourceFilePath = "src/main/resources/TestPath${json.readText().trim().md5()}.csv"

        val sourceFile = File(sourceFilePath)

        val sourceTrajectory: Trajectory

        if (!sourceFile.isFile) {
            val generationInfo = Gson().fromJson<PathGeneratorInfo>(FileReader(json))

            val config = Trajectory.Config(generationInfo.fitMethod, generationInfo.sampleRate, generationInfo.dt, generationInfo.vmax, generationInfo.amax, generationInfo.jmax)
            val waypoints = generationInfo.waypoints.toTypedArray()

            sourceTrajectory = Pathfinder.generate(waypoints, config)
            Pathfinder.writeToCSV(sourceFile, sourceTrajectory)
        } else {
            sourceTrajectory = Pathfinder.readFromCSV(sourceFile)
        }
        return sourceTrajectory
    }

    fun getPath(folder: String, file: String): Trajectory {
        return allPaths["$folder/$file Source"]!!
    }
}

data class PathGeneratorInfo(val dt: Double, val vmax: Double, val amax: Double, val jmax: Double, val wheelbasewidth: Double, val waypoints: ArrayList<Waypoint>, val fitMethod: Trajectory.FitMethod,
                             val sampleRate: Int)


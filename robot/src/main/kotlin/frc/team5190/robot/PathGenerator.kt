package frc.team5190.robot

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import frc.team5190.lib.md5
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import jaci.pathfinder.Waypoint
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.io.FileReader


object PathGenerator {

    private lateinit var allTrajectories: Map<String, Trajectory>

    val pathsGenerated: Boolean
        get() = try {
            allTrajectories.size == File("/home/lvuser/paths/Raw").listFiles().filter { file ->
                file.isFile && file.extension == "json"
            }.size
        } catch (e: Exception) {
            false
        }


    init {
        runBlocking {
            delay(1000)

            allTrajectories = File("/home/lvuser/paths/Raw").listFiles().filter { file ->
                file.isFile && file.extension == "json"
            }.map { file ->
                return@map file.nameWithoutExtension to generatePath(file.path).await()
            }.toMap()
            println("[PATHGENERATOR] Generation Completed")
        }
    }

    private fun generatePath(filepath: String) = async {


        val json = File(filepath)
        val rFilePath = "/home/lvuser/paths/${json.nameWithoutExtension}${json.readText().md5()}.csv"
        val wFilePath = "/home/lvuser/paths/${json.nameWithoutExtension}${json.readText().md5()}.csv"

        val rFile = File(rFilePath)
        val wFile = File(wFilePath)
        val trajectory: Trajectory


        if (!rFile.isFile) {
            println("[PATHGENERATOR] Generating ${json.nameWithoutExtension}")

            val parameters = Gson().fromJson<PathGeneratorInfo>(FileReader(json))
            val config = Trajectory.Config(
                    parameters.fitMethod,
                    parameters.sampleRate,
                    parameters.dt,
                    parameters.vmax,
                    parameters.amax,
                    parameters.jmax)

            val waypoints = parameters.waypoints.toTypedArray()
            trajectory = Pathfinder.generate(waypoints, config)
            Pathfinder.writeToCSV(wFile, trajectory)
        } else {
            println("[PATHGENERATOR] ${json.nameWithoutExtension} already exists. Using preloaded path.")
            trajectory = Pathfinder.readFromCSV(rFile)
        }
        return@async trajectory
    }

    fun getPath(filename: String): Trajectory {
        return allTrajectories[filename]!!
    }
}

data class PathGeneratorInfo(val dt: Double, val vmax: Double, val amax: Double, val jmax: Double, val wheelbasewidth: Double, val waypoints: ArrayList<Waypoint>, val fitMethod: Trajectory.FitMethod,
                             val sampleRate: Int)


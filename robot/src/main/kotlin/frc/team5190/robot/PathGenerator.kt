package frc.team5190.robot

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import frc.team5190.lib.extensions.md5
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import jaci.pathfinder.Waypoint
import kotlinx.coroutines.experimental.*
import java.io.File
import java.io.FileReader


object PathGenerator {

    // Context used for generating paths.
    private val generatorContext = newFixedThreadPoolContext(1, "Path Generation")

    // Use the keep track of all the path generation tasks.
    private val generatorJob = Job()

    // Map that contains a map of all paths.
    private val pathMap = mutableMapOf<String, Deferred<Trajectory>>()
    private val rawPathFolder = File("/home/lvuser/paths/Raw")

    init {
        val startTime = System.currentTimeMillis()
        println("[PathGenerator] Loading Paths...")

        // Generate one path for each JSON
        rawPathFolder.listFiles { it -> it.isFile && it.extension == "json" }.forEach { file ->
            pathMap[file.nameWithoutExtension] = generatePath(file.path)
        }

        // Asynchronous magic.
        launch {
            join()
            println("[PathGenerator] Finished Loading Paths. Job took ${System.currentTimeMillis() - startTime} ms")
        }
    }

    private fun generatePath(filepath: String) = async(context = generatorContext, parent = generatorJob) {
        val json = File(filepath)
        val file = File("/home/lvuser/paths/${json.nameWithoutExtension}${json.readText().md5()}.csv")

        val trajectory: Trajectory

        // Generate path if hash does not match, else load pre-generated path.
        if (!file.isFile) {
            val startTime = System.currentTimeMillis()

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
            Pathfinder.writeToCSV(file, trajectory)
            System.out.printf("[PathGenerator] %-31s%-5d ms%n", "\"${json.nameWithoutExtension}\" Time ->", System.currentTimeMillis() - startTime)
        } else {
            System.out.printf("[PathGenerator] Using preloaded version of %-20s %n", "\"${json.nameWithoutExtension}\"")
            trajectory = Pathfinder.readFromCSV(file)
        }
        return@async trajectory
    }

    // Returns the path.
    operator fun get(filename: String) = runBlocking {
        pathMap[filename]?.await()
    }

    // Waits for all path generation to complete.
    fun join() = runBlocking {
        pathMap.values.awaitAll()
    }
}

// Contains info for JSON parsing.
data class PathGeneratorInfo(val dt: Double,
                             val vmax: Double,
                             val amax: Double,
                             val jmax: Double,
                             val wheelbasewidth: Double,
                             val waypoints: ArrayList<Waypoint>,
                             val fitMethod: Trajectory.FitMethod,
                             val sampleRate: Int)


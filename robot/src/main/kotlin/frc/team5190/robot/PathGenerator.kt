package frc.team5190.robot

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import frc.team5190.lib.md5
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import jaci.pathfinder.Waypoint
import kotlinx.coroutines.experimental.*
import java.io.File
import java.io.FileReader


object PathGenerator {

    // Context used for generating paths
    private val generatorContext = newFixedThreadPoolContext(2, "Path Generation")

    // Use the keep track of all the path generation tasks
    private val generatorJob = Job()

    private val pathMap = mutableMapOf<String, Deferred<Trajectory>>()
    private val rawPathFolder = File("src/main/resources/Raw")

    init {
        val startTime = System.currentTimeMillis()
        println("[PathGenerator] Loading Paths...")

        rawPathFolder.listFiles { it -> it.isFile && it.extension == "json" }.forEach { file ->
            pathMap[file.nameWithoutExtension] = generatePath(file.path)
        }

        generatorJob.invokeOnCompletion {
            println("Finished Loading Paths. Job took ${System.currentTimeMillis() - startTime} ms")
        }

        runBlocking {
            pathMap.values.awaitAll()
        }
    }

    private fun generatePath(filepath: String) = async(context = generatorContext, parent = generatorJob) {
        val json = File(filepath)
        val file = File("src/main/resources/${json.nameWithoutExtension}${json.readText().md5()}.csv")

        val trajectory: Trajectory

        if (!file.isFile) {
            System.out.printf("[PATHGENERATOR] Generating %-20s...%n", "\"${json.nameWithoutExtension}\"")
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
            System.out.printf("[PATHGENERATOR] %-31s%-5d ms%n%n", "\"${json.nameWithoutExtension}\" Time ->", System.currentTimeMillis() - startTime)
        } else {
            System.out.printf("[PATHGENERATOR] Using preloaded version of %-20s %n", "\"${json.nameWithoutExtension}\"")
            trajectory = Pathfinder.readFromCSV(file)
        }
        return@async trajectory
    }

    operator fun get(filename: String) = runBlocking {
        pathMap[filename]?.await()
    }
}


data class PathGeneratorInfo(val dt: Double,
                             val vmax: Double,
                             val amax: Double,
                             val jmax: Double,
                             val wheelbasewidth: Double,
                             val waypoints: ArrayList<Waypoint>,
                             val fitMethod: Trajectory.FitMethod,
                             val sampleRate: Int)


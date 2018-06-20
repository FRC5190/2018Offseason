package frc.team5190.lib.util

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import frc.team5190.lib.md5
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import jaci.pathfinder.Waypoint
import jaci.pathfinder.modifiers.TankModifier
import kotlinx.coroutines.experimental.*
import java.io.File
import java.io.FileReader

object PathReader {

    /**
     * Context used for generating the paths
     */
    private val generatorContext = newFixedThreadPoolContext(2, "Path Generator")

    /**
     * Use the keep track of all the path generation tasks
     */
    private val pathGeneratorJob = Job()

    /**
     * Is true when there are no paths currently being generated
     */
    val hasPathGenerated
        get() = !pathGeneratorJob.isActive

    private val pathMap = mutableMapOf<PathKey, Deferred<PathGeneratorResult>>()

    private val rawPathFolder = File("/home/lvuser/paths/Raw")

    init {
        // Launch coroutines to asynchronous load the paths
        val startTime = System.currentTimeMillis()
        println("[PathGenerator] Loading paths...")
        rawPathFolder.listFiles { file -> file.isDirectory }.forEach { folder ->
            folder.listFiles { file -> file.isFile }.forEach { file ->
                val key = PathKey(folder.name, file.nameWithoutExtension)
                pathMap[key] = generatePath(key)
            }
        }
        pathGeneratorJob.invokeOnCompletion {
            println("[PathGenerator] Finished Loading Paths! (Took ${System.currentTimeMillis() - startTime}ms)")
        }
    }

    operator fun get(folder: String, file: String): PathGeneratorResult? = runBlocking {
        pathMap[PathKey(folder, file)]?.await()
    }

    private fun generatePath(key: PathKey) = async(context = generatorContext, parent = pathGeneratorJob) {
        val startTime = System.currentTimeMillis()
        println("[PathGenerator] [${key.folder}/${key.file}] Loading Path...")

        val folder = key.folder
        val file = key.file

        val json = File(rawPathFolder, "/$folder/$file.json")
        val jsonString = json.readText()

        val trajectoryFolderPath = "/home/lvuser/paths/$folder/${json.nameWithoutExtension}-${jsonString.md5()}"
        val leftFile = File("$trajectoryFolderPath Left Detailed.csv")
        val rightFile = File("$trajectoryFolderPath Right Detailed.csv")
        val sourceFile = File("$trajectoryFolderPath Source Detailed.csv")

        val leftTrajectory: Trajectory
        val rightTrajectory: Trajectory
        val sourceTrajectory: Trajectory

        if (!leftFile.isFile || !rightFile.isFile || !sourceFile.isFile) {
            println("[PathGenerator] [${key.folder}/${key.file}] Generating Path...")
            val generationInfo = Gson().fromJson<PathGeneratorInfo>(FileReader(json))
            val config = Trajectory.Config(generationInfo.fitMethod, generationInfo.sampleRate, generationInfo.dt, generationInfo.vmax, generationInfo.amax, generationInfo.jmax)

            val waypoints = generationInfo.waypoints.toTypedArray()

            val trajectory = Pathfinder.generate(waypoints, config)
            val modifier = TankModifier(trajectory)
            modifier.modify(generationInfo.wheelbasewidth)

            leftTrajectory = modifier.leftTrajectory
            rightTrajectory = modifier.rightTrajectory
            sourceTrajectory = modifier.sourceTrajectory

            Pathfinder.writeToCSV(sourceFile, sourceTrajectory)
            Pathfinder.writeToCSV(leftFile, leftTrajectory)
            Pathfinder.writeToCSV(rightFile, rightTrajectory)
            println("[PathGenerator] [${key.folder}/${key.file}] Generated path in ${System.currentTimeMillis()}ms...")
        } else {
            println("[PathGenerator] [${key.folder}/${key.file}] Loading path from file...")
            leftTrajectory = Pathfinder.readFromCSV(leftFile)
            rightTrajectory = Pathfinder.readFromCSV(rightFile)
            sourceTrajectory = Pathfinder.readFromCSV(sourceFile)
        }
        println("[PathGenerator] [${key.folder}/${key.file}] Path Loaded!")
        return@async PathGeneratorResult(sourceTrajectory, leftTrajectory, rightTrajectory)
    }

    data class PathKey(val folder: String,
                       val file: String)

    class PathGeneratorResult(val sourceTrajectory: Trajectory,
                              val leftTrajectory: Trajectory,
                              val rightTrajectory: Trajectory)
}

data class PathGeneratorInfo(val dt: Double, val vmax: Double, val amax: Double, val jmax: Double, val wheelbasewidth: Double, val waypoints: ArrayList<Waypoint>, val fitMethod: Trajectory.FitMethod, val sampleRate: Int)
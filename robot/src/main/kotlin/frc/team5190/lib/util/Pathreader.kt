package frc.team5190.lib.util

import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import java.io.File

object Pathreader {

    private val allPaths = File("/home/lvuser/paths/").listFiles().filter { it.isDirectory }.map { folder ->
        folder.listFiles().filter { it.isFile }.map { file ->
            Pair("${folder.name}/${file.nameWithoutExtension}", Pathfinder.readFromCSV(file))
        }
    }.flatten().toMap()

    var pathsGenerated = true
        private set


    fun getPath(folder: String, file: String): Trajectory {
        return allPaths["$folder/$file Source"]!!
    }
}


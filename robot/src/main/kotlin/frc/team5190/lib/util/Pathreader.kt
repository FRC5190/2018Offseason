package frc.team5190.lib.util

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import frc.team5190.lib.md5
import jaci.pathfinder.Pathfinder
import jaci.pathfinder.Trajectory
import jaci.pathfinder.Waypoint
import jaci.pathfinder.modifiers.TankModifier
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


    fun getPath(folder: String, file: String): Trajectory {
        return allPaths["$folder/$file Source"]!!
    }
}


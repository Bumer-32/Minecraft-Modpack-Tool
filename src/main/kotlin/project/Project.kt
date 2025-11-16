package ua.pp.lumivoid.project

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.data.ProjectData
import java.io.File
import kotlin.system.exitProcess

object Project {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val projectFolder = File(".")
    private val mmtProjectFolder = File(projectFolder, ".mmt")
    private val nogitFolder = File(mmtProjectFolder, "nogit")

    private val projectFile = File(mmtProjectFolder, "project.yaml")

    private val gameVersionFile = File(nogitFolder, "game.json")
    private val gameFolder = File(nogitFolder, "minecraft")

    private var cache: ProjectData? = null

    fun read(): ProjectData {
        if (cache != null) return cache!!

        if (!projectFile.exists()) {
            logger.error("Project file does not exist: ${projectFile.absolutePath}")
            exitProcess(1)
        }

        cache = Yaml.default.decodeFromString(projectFile.readText())
        return cache!!

    }

    fun getProjectFolder() = projectFolder
    fun getGameVersionFile() = gameVersionFile
    fun getGameFolder() = gameFolder
}
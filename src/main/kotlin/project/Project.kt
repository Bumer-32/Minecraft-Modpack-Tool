package ua.pp.lumivoid.project

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.data.ProjectData
import java.io.File

object Project {
    private val logger = LoggerFactory.getLogger(javaClass)

    private var cache: ProjectData? = null

    val projectFolder = File(".")
    val mmtProjectFolder = File(projectFolder, ".mmt")
    val nogitFolder = File(mmtProjectFolder, "nogit")

    val projectFile = File(mmtProjectFolder, "project.yaml")

    val gameFolder = File(nogitFolder, "minecraft")
    val tmpFolder = File(nogitFolder, "tmp")


    fun read(): ProjectData? {
        if (cache != null) return cache!!

        if (!projectFile.exists()) {
            logger.error("Project file does not exist: ${projectFile.absolutePath}")
            return null
        }

        cache = Yaml.default.decodeFromString(projectFile.readText())
        return cache!!

    }
}
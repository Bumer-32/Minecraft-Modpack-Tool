package ua.pp.lumivoid.tasks

import org.slf4j.LoggerFactory
import picocli.CommandLine
import ua.pp.lumivoid.project.Project
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "downloadGame",
    description = ["Downloads minecraft and libraries"]
)
object DownloadGame: Callable<Int> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun call(): Int {
        logger.info("Reading project...")
        if (Project.read() == null) return 1
        val project = Project.read()!!.project

        logger.info("Getting versions")
        val gameVersions = project.platform.realisation.getGameVersions()
        val loaderVersions = project.platform.realisation.getLoaderVersions()

        if (project.minecraft !in gameVersions) {
            logger.error("Minecraft ${project.minecraft} not found")
            return 1
        }
        if (project.loader !in loaderVersions) {
            logger.error("Loader ${project.loader} not found")
            return 1
        }

        logger.info("Clear old game")
        val versionsFolder = File(Project.gameFolder, "versions")
        versionsFolder.deleteRecursively()
        versionsFolder.mkdirs()
        val librariesFolder = File(Project.gameFolder, "libraries")
        librariesFolder.deleteRecursively()
        librariesFolder.mkdirs()

        logger.info("Starting installation...")
        val result = project.platform.realisation.install(project.minecraft, project.loader, Project.gameFolder)

        if (result == 0) {
            logger.info("Your game successfully installed!")
            return 0
        }
        else {
            logger.error("Error installing game $result")
            return result
        }
    }
}
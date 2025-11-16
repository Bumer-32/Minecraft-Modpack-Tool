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
        val project = Project.read().project

        logger.info("Clear old game")
        val versionsFolder = File(Project.getGameFolder(), "versions")
        versionsFolder.deleteRecursively()
        versionsFolder.mkdirs()
        Project.getGameVersionFile().delete()

        logger.info("Getting game versions")
        val versions = project.platform.realisation.getGameVersions()
        logger.info(versions.toString())

        return 0
    }
}
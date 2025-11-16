package ua.pp.lumivoid.tasks

import org.slf4j.LoggerFactory
import picocli.CommandLine
import ua.pp.lumivoid.project.Project
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "game",
    description = ["launching minecraft"]
)
object Game: Callable<Int> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun call(): Int {
        logger.info(Project.read().project.platform.platformName)

        return 0
    }
}
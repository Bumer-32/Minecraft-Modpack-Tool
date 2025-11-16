package ua.pp.lumivoid.tasks

import org.slf4j.LoggerFactory
import picocli.CommandLine
import ua.pp.lumivoid.project.Project
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "clean",
    description = ["Cleans nogit, tmp and cache files"]
)
object Clean: Callable<Int> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun call(): Int {
        logger.info("Task: clean...")

        Project.nogitFolder.deleteRecursively()

        return 0
    }
}
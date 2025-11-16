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

    @CommandLine.Option(names = ["-a", "--args"], description = ["Custom arguments for minecraft"])
    private var mcArgs = ""

    @CommandLine.Option(names = ["-j", "--jvmArgs"], description = ["Custom arguments for jvm"])
    private var jvmArgs = ""

    @CommandLine.Option(names = ["-f", "--force"], description = ["Skips checks"])
    private var force = false

    @CommandLine.Option(names = ["-d", "--dontDownload"], description = ["Stops task if game check failed instead of downloading it"])
    private var dontDownload = false


    override fun call(): Int {
        logger.info("Task: launch game...")

        logger.info("Reading project")
        if (Project.read() == null) return 1
        val project = Project.read()!!.project

        val check = if (!force) {
            logger.info("Checking minecraft installation")
//            project.platform.realisation.check()
        } else true

//        if (!check && !dontDownload) {
//            logger.info("Minecraft not found, reinstalling")
//            val result = DownloadGame.call()
//            if (result != 0) return result
//        }

//        if (!check && dontDownload) {
//            logger.error("Minecraft not found")
//            return 1
//        }

        logger.info("Launching")
//        project.platform.realisation.launch(mcArgs, jvmArgs, project.minecraft, project.loader, Project.gameFolder)

        return 0
    }
}
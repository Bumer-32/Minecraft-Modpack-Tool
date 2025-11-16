package ua.pp.lumivoid.tasks

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import picocli.CommandLine
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.project.platform.Platforms
import ua.pp.lumivoid.util.CheckInput
import ua.pp.lumivoid.util.Zip
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "init",
    description = ["Initializes project creation"]
)
object Init: Callable<Int> {
    private val logger = LoggerFactory.getLogger(javaClass)

    @CommandLine.Option(names = ["-p", "--path"], description = ["Path where project will be created (folder must be empty if not --force)"])
    private var path: File? = null

    @CommandLine.Option(names = ["-n", "--name"], description = ["Name of the project"])
    private var name: String? = null

    @CommandLine.Option(names = ["-a", "--author"], description = ["Author of the project"])
    private var author: String? = null

    @CommandLine.Option(names = ["-P", "--platform"], description = ["Platform of the project, e.g. fabric"])
    private var platform: String? = null

    @CommandLine.Option(names = ["-g", "--game", "-v"], description = ["Version of minecraft, e.g. 1.21.1"])
    private var game: String? = null

    @CommandLine.Option(names = ["-l", "--loader"], description = ["Version of loader, e.g. 0.18.0"])
    private var loader: String? = null

    @CommandLine.Option(names = ["--ilovedogs", "--nocats"], description = ["If you hate cats, cat screenshot will not be created in readme"])
    private var noCats = false

    @CommandLine.Option(names = ["-f", "--force"], description = ["Forces project creation if it's possible"])
    private var force = false

    @CommandLine.Option(names = ["-y", "--yes", "--confirm"], description = ["If yes, you don't need to answer is your information correct"])
    private var confirm = false

    override fun call(): Int {
        logger.info("Task: init")

        do {
            var fail = false

            path = CheckInput.checkFile("Folder with project (${path?.absolutePath})", path)
            name = CheckInput.checkFilledStr("Project name", name).trim()
            author = CheckInput.checkStr("Author", author).trim()
            platform = CheckInput.checkChoosable("Platform", Platforms.entries.map { it.platformName } , platform)
            game = CheckInput.checkFilledStr("Minecraft version", game).trim()
            loader = CheckInput.checkFilledStr("Loader version", loader).trim()

            val actualPlatform = Platforms.entries.find { it.platformName == platform }!!

            if (game !in actualPlatform.realisation.getGameVersions()) {
                logger.error("Minecraft $game not found")
                fail = true
            }
            if (loader !in actualPlatform.realisation.getLoaderVersions()) {
                logger.error("Loader $loader not found")
                fail = true
            }

            logger.info("path: ${path!!.absolutePath}")
            logger.info("name: $name")
            logger.info("author: $author")
            logger.info("path: $path")
            logger.info("platform: $platform")
            logger.info("minecraft: $game")
            logger.info("loader: $loader")

            if (!confirm && !fail) {
                val correct = CheckInput.confirm("Is all information correct?")
                if (!correct) fail = true
                else confirm = true
            }

            if (fail) {
                path = null
                name = null
                author = null
                platform = null
                game = null
                loader = null
                confirm = false
            }
        } while (!confirm)

        if (path!!.listFiles() != null && path!!.listFiles().isNotEmpty() && !force) {
            logger.error("Folder is not empty!")
            return 1
        }

        logger.info("Creating project...")
        generate(
            path!!,
            noCats,
            mapOf(
                "name" to name!!,
                "author" to author!!,
                "platform" to platform!!,
                "minecraft" to game!!,
                "loader" to loader!!
            )
        )

        return 0
    }


    private fun generate(path: File, noCats: Boolean, params: Map<String, String>) {
        val httpClient = Constants.httpClient

        logger.info("Coping sample.zip...")
        path.mkdirs()
        val sample = File(path, "sample.zip")
        sample.writeBytes(javaClass.getResource("/sample.zip")!!.readBytes())
        Zip.unzipToFolder(sample, path)
        sample.delete()

        logger.info("Applying params...")
        path.walk().forEach { file ->
            if (file.isFile) {
                val lines = file.readLines().toMutableList()
                lines.forEachIndexed { i, line ->
                    params.forEach { param ->
                        if (line.contains("^{${param.key}}^")) {
                            lines[i] = line.replace("^{${param.key}}^", param.value)
                        }
                    }
                }
                file.writeText(lines.joinToString("\n"))
            }
        }


        if (!noCats) {
            runCatching { // try, if fail (e.g. no internet) then just remove ^{cat}^
                logger.info("Meow!")
                runBlocking {
                    val response = httpClient.get("https://cataas.com/cat")
                    val catFile = File(path, "screenshots/meow.jpg")
                    catFile.parentFile.mkdirs()
                    catFile.createNewFile()
                    catFile.writeBytes(response.body())
                }

                val readme = File(path, "README.md")
                val lines = readme.readLines().toMutableList()
                val index = lines.withIndex().find { it.value.contains("^{cat}^") }?.index
                if (index != null) {
                    lines[index] = "<img src=\"screenshots/meow.jpg\">"
                }
                readme.writeText(lines.joinToString("\n"))
            }
        }

        // if cat downloading failed or --nocats then we need to remove ^{cat}^
        val readme = File(path, "README.md")
        val lines = readme.readLines().toMutableList()
        val index = lines.withIndex().find { it.value.contains("^{cat}^") }?.index
        if (index != null) {
            lines[index] = ""
        }
        readme.writeText(lines.joinToString("\n"))

        logger.info("")
        logger.info("")
        logger.info("")
        logger.info("Your project has been created")
        logger.info("Please read README.md first!")

    }
}
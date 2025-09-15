package ua.pp.lumivoid.tasks.provided

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.tasks.Task
import ua.pp.lumivoid.tasks.TaskArgument
import ua.pp.lumivoid.util.Zip
import java.io.File

object Init: Task(
    "init",
    "Initializes project creation",
    listOf(
        TaskArgument(
            name = "path",
            description = "Path where project will be created (folder must be empty if not --force)",
            required = true,
            isNotEmpty = true,
            hasValue = true,
            aliases = listOf("-p", "--path"),
            default = System.getProperty("user.dir").replace("\\", "/"),
            validator = { input -> !runCatching { File(input) }.isFailure }
        ),
        TaskArgument(
            name = "name",
            description = "Specifies name of project",
            required = true,
            isNotEmpty = true,
            hasValue = true,
            listOf("-n", "--name")
        ),
        TaskArgument(
            name = "author",
            description = "Specifies author of project",
            required = true,
            isNotEmpty = false,
            hasValue = true,
            listOf("-a" , "--author")
        ),
        TaskArgument(
            name = "confirm",
            description = "If yes, you don't need to answer is your information correct",
            required = false,
            hasValue = false,
            aliases = listOf("-y"),
        ),
        TaskArgument(
            name = "noCats",
            description = "If you hate cats, cat screenshot will not be created in readme",
            required = false,
            hasValue = false,
            aliases = listOf("--ilovedogs", "--nocats")
        ),
        TaskArgument(
            name = "force",
            description = "Forces to create if possible",
            required = false,
            hasValue = false,
            aliases = listOf("-f", "--force")
        )
    )
) {
    override fun call(args: List<String>) {
        logger.debug("Args: ${args.joinToString(" ")}")

        var path: File
        var name: String
        var author: String
        val noCats = readArg("noCats", args) != null
        val force = readArg("force", args) != null

        val confirmed: String? = readArg("confirm", args)

        do {
            path = File(readArg("path", args)!!)
            name = readArg("name", args)!!
            author = readArg("author", args)!!

            logger.info("name: $name")
            logger.info("path: ${path.absolutePath}")
            logger.info("author: $author")

            val correct = if (confirmed == null) checkIsCorrect() else true
        } while (!correct)

        if (path.listFiles().isNotEmpty() && !force) {
            logger.error("Folder is not empty!")
            return
        }

        logger.info("Creating project...")
        generate(path,
            noCats,
            mapOf(
                "name" to name,
                "author" to author,
                "platform" to "fabric",
                "minecraft" to "1.21.8",
                "loader" to "0.17.2"

            )
        )
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

        logger.info("")
        logger.info("")
        logger.info("")
        logger.info("Your project has been created")
        logger.info("Please read README.md first!")

    }
}
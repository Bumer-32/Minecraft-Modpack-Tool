package ua.pp.lumivoid.tasks.provided

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.tasks.Task
import ua.pp.lumivoid.tasks.TaskArgument
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
        generate(name, path, author, noCats)
    }


    private fun generate(name: String, path: File, author: String, noCats: Boolean) {
        // For now it's just a example of project generation, there's even no platform selection!
        val httpClient = Constants.httpClient

        val readmeText = """
            # $name  
            by: $author  
            
            This is a project created by [Minecraft Modpack Tool by Bumer_32](https://github.com/Bumer-32/Minecraft-Modpack-Tool)  
            Now there's nothing interesting, but in future it must be a cool project
            
            ---
            <!-- exclude.start -->
            This block will be excluded on build
            <!-- exclude.end -->
            
            <!-- modrinth.exclude.start -->
            This block will be excluded on modrinth
            <!-- modrinth.exclude.end -->
            
            <!-- curseforge.exclude.start -->
            This block will be excluded on curseforge
            <!-- curseforge.exclude.end -->
            
            
           ---
           Please leave at least some information about the tool with which Modpack was created ^_^
           ${if (!noCats) "<img src=\"screenshots/meow.jpg\">" else ""}
        """.trimIndent()

        logger.info("Writing readme")
        val readmeFile = File(path, "README.md")
        readmeFile.parentFile.mkdirs()
        readmeFile.createNewFile()
        readmeFile.writeText(readmeText)

        if (!noCats) {
            logger.info("Meow!")
            runBlocking {
                val response = httpClient.get("https://cataas.com/cat")
                val catFile = File(path, "screenshots/meow.jpg")
                catFile.parentFile.mkdirs()
                catFile.createNewFile()
                catFile.writeBytes(response.body())
            }
        }

        logger.info("")
        logger.info("")
        logger.info("")
        logger.info("Your project has been created")
        logger.info("Please read README.md first!")

    }
}
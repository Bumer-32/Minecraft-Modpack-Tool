package ua.pp.lumivoid.tasks.provided

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.tasks.Task
import java.io.File

object Init: Task(
    "init",
    "Initializes project",
) {
    override fun call() {
        while (true) {
            var path: File? = null
            var name: String
            var author: String

            do {
                logger.info("Select name: ")
                name = readln()
            } while (name.isEmpty())

            do {
                val result = runCatching {
                    print("Select directory[default: ${System.getProperty("user.dir").replace("\\", "/")}/$name]: ")
                    path = File(readln().ifEmpty { name })
                }
            } while (result.isFailure)

            logger.info("Select author: ")
            author = readln()

            logger.info("name: $name")
            logger.info("path: ${path!!.absolutePath}")
            logger.info("author: $author")

            logger.info("Is all information correct? [y/n]")
            val isCorrect = readln().lowercase()
            if (isCorrect  != "y" && isCorrect != "yes") {
                logger.info("Try again:")
                continue
            }

            logger.info("Creating project...")
            generate(name, path, author)

            break
        }
    }


    private fun generate(name: String, path: File, author: String) {
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
           <img src="screenshots/meow.jpg">
        """.trimIndent()

        logger.info("Writing readme")
        val readmeFile = File(path, "README.md")
        readmeFile.parentFile.mkdirs()
        readmeFile.createNewFile()
        readmeFile.writeText(readmeText)

        logger.info("Meow!")
        runBlocking {
            val response = httpClient.get("https://cataas.com/cat")
            val catFile = File(path, "screenshots/meow.jpg")
            catFile.parentFile.mkdirs()
            catFile.createNewFile()
            catFile.writeBytes(response.body())
        }

        logger.info("")
        logger.info("")
        logger.info("")
        logger.info("Your project has been created")
        logger.info("Please read README.md first!")

    }
}
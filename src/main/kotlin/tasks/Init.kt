package ua.pp.lumivoid.tasks

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import picocli.CommandLine
import ua.pp.lumivoid.Constants
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

    @CommandLine.Option(names = ["--ilovedogs", "--nocats"], description = ["If you hate cats, cat screenshot will not be created in readme"])
    private var noCats = false

    @CommandLine.Option(names = ["-f", "--force"], description = ["Forces project creation if it's possible"])
    private var force = false

    @CommandLine.Option(names = ["-y", "--yes", "--confirm"], description = ["If yes, you don't need to answer is your information correct"])
    private var confirm = false

    override fun call(): Int {
//        logger.info("""
//            path: ${path?.absolutePath}
//            name: $name
//            author: $author
//            noCats: $noCats
//            force: $force
//            confirm: $confirm
//
//        """.trimIndent())
//        do {
//            path = File(readArg("path", args)!!)
//            name = readArg("name", args)!!
//            author = readArg("author", args)!!
//
//            logger.info("name: $name")
//            logger.info("path: ${path.absolutePath}")
//            logger.info("author: $author")
//
//            val correct = if (confirm == null) checkIsCorrect() else true
//        } while (!correct)

        if (path!!.listFiles().isNotEmpty() && !force) {
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
                "platform" to "fabric",
                "minecraft" to "1.21.8",
                "loader" to "0.17.2"
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
package ua.pp.lumivoid.tasks

import org.slf4j.LoggerFactory
import picocli.CommandLine
import ua.pp.lumivoid.util.Zip
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "systemInstall",
    description = ["Install mmt on system"]
)
object SystemInstall: Callable<Int> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun call(): Int {
        logger.info("Task: install...")

        val installFolder = File(System.getProperty("user.home"), ".mmt")
        val installMmtFile = File(installFolder, "mmt.jar")
        if (installMmtFile.exists()) {
            logger.warn("mmt.jar already exists: ${installMmtFile.absolutePath}")
            logger.warn("Overwrite? (y/n)")
            if (readln().first().lowercase() != "y") return 0
        }

        logger.info("Creating folders: ${installFolder.absolutePath}")
        installFolder.mkdirs()

        logger.info("Copying mmt.jar")
        File(javaClass.protectionDomain.codeSource.location.toURI()).copyTo(installMmtFile, overwrite = true)

        logger.info("Copying install.zip")
        val install = File(installFolder, "install.zip")
        install.writeBytes(javaClass.getResource("/install.zip")!!.readBytes())
        Zip.unzipToFolder(install, installFolder)
        install.delete()

        logger.info("Installation complete!")
        logger.info("")

        repeat(5) {
            logger.info("Please add ${installFolder.absolutePath} to system PATH")
        }

        logger.info("")
        return 0
    }
}
package ua.pp.lumivoid.project.platform

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.data.MinecraftJson
import ua.pp.lumivoid.data.fabric.FabricJson
import ua.pp.lumivoid.data.fabric.Game
import ua.pp.lumivoid.data.fabric.Loader
import ua.pp.lumivoid.util.OS
import java.io.File

@Suppress("DuplicatedCode", "LoggingSimilarMessage")
object Fabric: Platform() {
    private const val META_GAME_URL = "https://meta.fabricmc.net/v2/versions/game"
    private const val META_LOADER_URL = "https://meta.fabricmc.net/v2/versions/loader"

    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = Constants.httpClient

    private var gameVersionsCache: Map<String, Boolean>? = null
    private var loaderVersionsCache: Map<String, Boolean>? = null

    override fun getGameVersions(): Map<String, Boolean> = runBlocking {
        if (gameVersionsCache != null) return@runBlocking gameVersionsCache!!
        val response = httpClient.get(META_GAME_URL)
        val versions = Game.parse(response.body())
        val map = mutableMapOf<String, Boolean>()
        versions.forEach { version ->
            map[version.version] = version.stable
        }
        gameVersionsCache = map
        return@runBlocking map
    }

    override fun getLoaderVersions(): Map<String, Boolean> = runBlocking {
        if (loaderVersionsCache != null) return@runBlocking loaderVersionsCache!!
        val response = httpClient.get(META_LOADER_URL)
        val versions = Loader.parse(response.body())
        val map = mutableMapOf<String, Boolean>()
        versions.forEach { version ->
            map[version.version] = version.stable
        }
        loaderVersionsCache = map
        return@runBlocking map
    }

    override fun install(version: String, loaderVersion: String, path: File): Boolean = runBlocking {
        val mcFolder = File(path, "versions/fabric-loader-$loaderVersion-$version")
        val librariesFolder = File(path, "libraries")

        val fabricLoaderUrl = "$META_LOADER_URL/$version/$loaderVersion"
        val fabricProfileUrl = "$fabricLoaderUrl/profile/json"

        logger.info("Installing minecraft")
        installVanillaVersion(version, loaderVersion, path)

        // ? FABRIC
        logger.info("Downloading fabric profile...")
        val fabricProfileFile = File(mcFolder, "fabric-loader-$loaderVersion-$version.json")
        val fabricProfileResponse = httpClient.get(fabricProfileUrl)
        fabricProfileFile.writeBytes(fabricProfileResponse.bodyAsBytes())
        val fabricProfile = FabricJson.parse(fabricProfileResponse.body())

        logger.info("Downloading fabric libraries...")
        fabricProfile.libraries.forEach { library ->
            logger.info("   Downloading fabric library ${library.name}...")
            val separated = library.name.split(":")
            // org.ow2.asm:asm:9.9 -> org/ow2/asm/asm/9.9/asm-9.9.jar
            val path = "${separated[0].replace(".", "/")}/${separated[1]}/${separated[2]}/${separated[1]}-${separated[2]}.jar"
            val file = File(librariesFolder, path)
            file.parentFile.mkdirs()
            val response = httpClient.get("${library.url}$path")
            file.writeBytes(response.bodyAsBytes())
            val hashResponse = httpClient.get("${library.url}$path.sha1") // download sha1 from maven because sometimes it's missing in profile
            if (DigestUtils.sha1Hex(file.readBytes()) != hashResponse.body<String>()) {
                logger.error("Error during downloading fabric library ${library.name}")
                return@runBlocking false
            }

        }

        return@runBlocking true
    }

    override fun launch(mcArgs: String, jvmArgs: String, version: String, loaderVersion: String, path: File) {
        TODO("Not yet implemented")
    }

    /*
    * Checks only is file exists
    */
    override fun fastCheck(version: String, loaderVersion: String, path: File): Boolean = runBlocking {
        logger.info("Getting all info...")
        val mcFolder = File(path, "versions/$version")
        val fabricFolder = File(path, "versions/fabric-loader-$loaderVersion-$version")
        val librariesFolder = File(path, "libraries")

        val mcProfileFile = File(mcFolder, "$version.json")
        val mcFile = File(mcFolder, "$version.jar")
        val fabricProfileFile = File(fabricFolder, "fabric-loader-$loaderVersion-$version.json")

        val mcProfile = MinecraftJson.parse(mcProfileFile.readText())
        val fabricProfile = FabricJson.parse(fabricProfileFile.readText())

        logger.info("Checking minecraft")
        if (!mcFile.exists()) return@runBlocking false

        logger.info("Checking libraries")
        mcProfile.libraries.forEach { library ->
            if (library.rules != null) {
                if (library.rules.first().os.name != OS.current()) return@forEach
            }

            val file = File(librariesFolder,library.downloads.artifact.path)
            if (!file.exists()) return@runBlocking false
        }

        logger.info("Checking fabric libraries")
        fabricProfile.libraries.forEach { library ->
            val separated = library.name.split(":")
            // org.ow2.asm:asm:9.9 -> org/ow2/asm/asm/9.9/asm-9.9.jar
            val path = "${separated[0].replace(".", "/")}/${separated[1]}/${separated[2]}/${separated[1]}-${separated[2]}.jar"
            val file = File(librariesFolder, path)
            if (!file.exists()) return@runBlocking false
        }

        return@runBlocking true
    }
}
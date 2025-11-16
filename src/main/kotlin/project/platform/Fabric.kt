package ua.pp.lumivoid.project.platform

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.data.MinecraftJson
import ua.pp.lumivoid.data.MinecraftVersionManifestJson
import ua.pp.lumivoid.data.fabric.FabricJson
import ua.pp.lumivoid.data.fabric.Game
import ua.pp.lumivoid.data.fabric.Loader
import ua.pp.lumivoid.project.Project
import ua.pp.lumivoid.util.OS
import ua.pp.lumivoid.util.Zip
import java.io.File


object Fabric: Platform {
    private const val META_MINECRAFT_URL = "https://piston-meta.mojang.com/mc/game/version_manifest.json"
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

    override fun install(version: String, loaderVersion: String, path: File): Int = runBlocking {
        val mcFolder = File(path, "versions/$version")
        val fabricFolder = File(path, "versions/fabric-loader-$loaderVersion-$version")
        val librariesFolder = File(path, "libraries")

        val fabricUrl = "$META_LOADER_URL/$version/$loaderVersion/profile/zip"

        // ? MINECRAFT
        logger.info("Downloading minecraft version manifest...")
        val mcVersionsResponse = httpClient.get(META_MINECRAFT_URL)
        val mcVersions = MinecraftVersionManifestJson.parse(mcVersionsResponse.body())
        val mcVersionManifest = mcVersions.versions.find { it.id == version }!!

        logger.info("Creating version folders...")
        mcFolder.mkdirs()
        fabricFolder.mkdirs()

        logger.info("Downloading minecraft profile...")
        val mcProfileFile = File(mcFolder, "$version.json")
        val mcProfileResponse = httpClient.get(mcVersionManifest.url)
        mcProfileFile.writeBytes(mcProfileResponse.bodyAsBytes())
        val mcProfile = MinecraftJson.parse(mcProfileResponse.body())

        logger.info("Downloading minecraft...")
        val mcFile = File(mcFolder, "$version.jar")
        val mcResponse = httpClient.get(mcProfile.downloads.client.url)
        mcFile.writeBytes(mcResponse.bodyAsBytes())
        if (mcFile.totalSpace != mcProfile.downloads.client.size && DigestUtils.sha1Hex(mcFile.readBytes()) != mcProfile.downloads.client.sha1) {
            logger.error("Error during download minecraft")
            return@runBlocking 1
        }

        logger.info("Downloading minecraft libraries...")
        mcProfile.libraries.forEach { library ->
            if (library.rules != null) {
                if (library.rules.first().os.name != OS.current()) return@forEach
            }

            logger.info("   Downloading minecraft library ${library.name}...")
            val file = File(librariesFolder, library.downloads.artifact.path)
            file.parentFile.mkdirs()
            val response = httpClient.get(library.downloads.artifact.url)
            file.writeBytes(response.bodyAsBytes())
            if (file.totalSpace != library.downloads.artifact.size && DigestUtils.sha1Hex(file.readBytes()) != library.downloads.artifact.sha1) {
                logger.error("Error during downloading minecraft library ${library.name}")
                return@runBlocking 1
            }
        }

        // ? FABRIC
        logger.info("Downloading fabric...")
        val fabricZipFile = File(Project.tmpFolder, "fabric.zip")
        fabricZipFile.parentFile.mkdirs()
        val fabricResponse = httpClient.get(fabricUrl)
        fabricZipFile.writeBytes(fabricResponse.bodyAsBytes())
        Zip.unzipToFolder(fabricZipFile, fabricFolder.parentFile)
        fabricZipFile.delete()

        logger.info("Downloading fabric libraries...")
        val fabricProfileFile = File(fabricFolder, "fabric-loader-$loaderVersion-$version.json")
        val fabricProfile = FabricJson.parse(fabricProfileFile.readText())
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
                return@runBlocking 1
            }

        }

        // ? FINISHED
        val totalSpace = mcFolder.totalSpace + fabricFolder.totalSpace + librariesFolder.totalSpace
        logger.info("Total space occupied: $totalSpace bytes")

        return@runBlocking 0
    }
}
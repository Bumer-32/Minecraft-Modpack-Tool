package ua.pp.lumivoid.project.platform

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.data.AssetIndexJson
import ua.pp.lumivoid.data.MinecraftJson
import ua.pp.lumivoid.data.MinecraftVersionManifestJson
import ua.pp.lumivoid.data.fabric.FabricJson
import ua.pp.lumivoid.data.fabric.Game
import ua.pp.lumivoid.data.fabric.Loader
import ua.pp.lumivoid.util.OS
import java.io.File

@Suppress("DuplicatedCode", "LoggingSimilarMessage")
object Fabric: Platform {
    private const val META_MINECRAFT_URL = "https://piston-meta.mojang.com/mc/game/version_manifest.json"
    private const val ASSETS_MINECRAFT_URL = "https://resources.download.minecraft.net"
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
        val assetsFolder = File(path, "assets")

        val fabricLoaderUrl = "$META_LOADER_URL/$version/$loaderVersion"
        val fabricProfileUrl = "$fabricLoaderUrl/profile/json"

        var error = false

        // ? MINECRAFT
        logger.info("Downloading minecraft version manifest...")
        val mcVersionsResponse = httpClient.get(META_MINECRAFT_URL)
        val mcVersions = MinecraftVersionManifestJson.parse(mcVersionsResponse.body())
        val mcVersionManifest = mcVersions.versions.find { it.id == version }!!

        logger.info("Creating version folder...")
        mcFolder.mkdirs()

        logger.info("Downloading minecraft profile...")
        val mcProfileFile = File(mcFolder, "$version.json")
        val mcProfileResponse = httpClient.get(mcVersionManifest.url)
        mcProfileFile.writeBytes(mcProfileResponse.bodyAsBytes())
        val mcProfile = MinecraftJson.parse(mcProfileResponse.body())

        logger.info("Downloading minecraft...")
        val mcFile = File(mcFolder, "$version.jar")
        val mcResponse = httpClient.get(mcProfile.downloads.client.url)
        val mcBytes = mcResponse.bodyAsBytes()
        mcFile.writeBytes(mcBytes)
        if (mcFile.totalSpace != mcProfile.downloads.client.size && DigestUtils.sha1Hex(mcBytes) != mcProfile.downloads.client.sha1) {
            logger.error("Error during download minecraft")
            return@runBlocking false
        }

        logger.info("Downloading minecraft libraries...")
        mcProfile.libraries.map { library ->
            async(Dispatchers.IO) {
                if (library.rules != null) {
                    if (library.rules.first().os.name != OS.current()) return@async
                }

                logger.info("   Downloading minecraft library ${library.name}...")
                val file = File(librariesFolder, library.downloads.artifact.path)
                file.parentFile.mkdirs()
                val response = httpClient.get(library.downloads.artifact.url)
                val bytes = response.bodyAsBytes()
                file.writeBytes(bytes)
                if (file.totalSpace != library.downloads.artifact.size && DigestUtils.sha1Hex(bytes) != library.downloads.artifact.sha1) {
                    logger.error("Error during downloading minecraft library ${library.name}")
                    error = true
                }
            }
        }.awaitAll()

        if (error) return@runBlocking false

        logger.info("Downloading minecraft assets..")
        val indexFile = File(assetsFolder, "indexes/${mcProfile.assetIndex.id}.json")
        indexFile.parentFile.mkdirs()
        val indexResponse = httpClient.get(mcProfile.assetIndex.url)
        val indexBytes = indexResponse.bodyAsBytes()
        indexFile.writeBytes(indexBytes)
        if (indexFile.totalSpace != mcProfile.assetIndex.size && DigestUtils.sha1Hex(indexBytes) != mcProfile.assetIndex.sha1) {
            logger.error("Error during downloading minecraft asset index")
            return@runBlocking false
        }

        val semaphore = Semaphore(16)
        AssetIndexJson.parse(indexFile.readText()).objects.values.map { asset ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    logger.info("Downloading asset ${asset.hash}")
                    val file = File(assetsFolder, "objects/${asset.hash.take(2)}/${asset.hash}")
                    val request = httpClient.get("$ASSETS_MINECRAFT_URL/${asset.hash.take(2)}/${asset.hash}")
                    val bytes = request.bodyAsBytes()
                    file.parentFile.mkdirs()
                    file.writeBytes(bytes)
                    if (file.totalSpace != asset.size && DigestUtils.sha1Hex(bytes) != asset.hash) {
                        logger.error("Error during downloading minecraft asset ${asset.hash}")
                        error = true
                    }
                }
            }
        }.awaitAll()

        if (error) return@runBlocking false

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

        // ? FINISHED
        val totalSpace = mcFolder.totalSpace + mcFolder.totalSpace + librariesFolder.totalSpace
        logger.info("Total space occupied: $totalSpace bytes")

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
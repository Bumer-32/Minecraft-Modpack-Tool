package ua.pp.lumivoid.project.platform

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
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

import ua.pp.lumivoid.util.OS
import java.io.File

abstract class Platform {
    private val manifestUrl = "https://piston-meta.mojang.com/mc/game/version_manifest.json"
    private val assetsUrl = "https://resources.download.minecraft.net"

    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = Constants.httpClient

    abstract fun getGameVersions(): Map<String, Boolean>
    abstract fun getLoaderVersions(): Map<String, Boolean>
    abstract fun install(version: String, loaderVersion: String, path: File): Boolean
    abstract fun launch(mcArgs: String, jvmArgs: String, version: String, loaderVersion: String, path: File)
    abstract fun fastCheck(version: String, loaderVersion: String, path: File): Boolean

    protected fun installVanillaVersion(version: String, loaderVersion: String, path: File): Boolean = runBlocking {
        val mcFolder = File(path, "versions/fabric-loader-$loaderVersion-$version")
        val librariesFolder = File(path, "libraries")
        val assetsFolder = File(path, "assets")

        var error = false

        // ? MINECRAFT
        logger.info("Downloading minecraft version manifest...")
        val mcVersionsResponse = httpClient.get(manifestUrl)
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
                    val request = httpClient.get("$assetsUrl/${asset.hash.take(2)}/${asset.hash}")
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

        return@runBlocking true
    }
}
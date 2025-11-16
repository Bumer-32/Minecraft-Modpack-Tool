package ua.pp.lumivoid.project.platform

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.data.fabric.Game
import ua.pp.lumivoid.data.fabric.Loader


object Fabric: Platform {
    private const val META_GAME_URL = "https://meta.fabricmc.net/v2/versions/game"
    private const val META_LOADER_URL = "https://meta.fabricmc.net/v2/versions/loader"

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

    override fun install(version: String, loaderVersion: String) {
        TODO("Not yet implemented")
    }

    override fun check(): Boolean {
        return true
    }

    override fun libraries() {
        TODO("Not yet implemented")
    }
}
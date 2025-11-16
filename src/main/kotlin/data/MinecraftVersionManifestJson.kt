package ua.pp.lumivoid.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// https://piston-meta.mojang.com/mc/game/version_manifest.json

@Serializable
data class MinecraftVersionManifestJson(
    val versions: List<Version>
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun parse(str: String): MinecraftVersionManifestJson {
            return json.decodeFromString<MinecraftVersionManifestJson>(str)
        }
    }

    @Serializable
    data class Version(
        val id: String,
        val type: String,
        val url: String,
    )
}

package ua.pp.lumivoid.data.fabric

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// https://meta.fabricmc.net/v2/versions/loader/1.21.1/0.18.0/profile/json

@Serializable
data class FabricJson(
    val id: String,
    val inheritsFrom: String,
    val mainClass: String,
    val libraries: List<Library>,
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun parse(str: String): FabricJson {
            return json.decodeFromString<FabricJson>(str)
        }
    }

    @Serializable
    data class Library(
        val name: String,
        val url: String,
        val sha1: String? = null, // sometimes it's missing
        val size: Long? = null
    )

}

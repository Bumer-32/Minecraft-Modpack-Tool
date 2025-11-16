package ua.pp.lumivoid.data.fabric

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// https://meta.fabricmc.net/v2/versions/game

@Serializable
data class Game(
    val version: String,
    val stable: Boolean,
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun parse(str: String): List<Game> {
            return json.decodeFromString<List<Game>>(str)
        }
    }
}

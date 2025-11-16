package ua.pp.lumivoid.data.fabric

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Loader(
    val version: String,
    val stable: Boolean
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun parse(str: String): List<Loader> {
            return json.decodeFromString<List<Loader>>(str)
        }
    }
}

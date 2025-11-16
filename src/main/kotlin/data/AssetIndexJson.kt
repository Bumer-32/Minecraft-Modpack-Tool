package ua.pp.lumivoid.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AssetIndexJson(
    val objects: Map<String, Asset>
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun parse(str: String): AssetIndexJson {
            return json.decodeFromString<AssetIndexJson>(str)
        }
    }

    @Serializable
    data class Asset(
        val hash: String,
        val size: Long
    )
}

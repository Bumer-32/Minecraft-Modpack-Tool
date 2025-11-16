package ua.pp.lumivoid.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// https://piston-meta.mojang.com/v1/packages/a112787401cbfa9cafc848d81be2f87e3f760e3e/1.21.8.json

@Serializable
data class MinecraftJson(
    val assetIndex: AssetIndex,
    val downloads: Downloads,
    val id: String,
    val libraries: List<Library>,
    val mainClass: String
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun parse(str: String): MinecraftJson {
            return json.decodeFromString<MinecraftJson>(str)
        }
    }

    @Serializable
    data class AssetIndex(
        val id: String,
        val sha1: String,
        val size: Long,
        val totalSize: Long,
        val url: String
    )

    @Serializable
    data class Downloads(
        val client: Client
    ) {
        @Serializable
        data class Client(
            val sha1: String,
            val size: Long,
            val url: String
        )
    }

    @Serializable
    data class Library(
        val downloads: Downloads,
        val name: String,
        val rules: List<Rule>? = null
    ) {
        @Serializable
        data class Downloads(
            val artifact: Artifact
        ) {
            @Serializable
            data class Artifact(
                val path: String,
                val sha1: String,
                val size: Long,
                val url: String
            )
        }

        @Serializable
        data class Rule(
            val action: String,
            val os: OS
        ) {
            @Serializable
            data class OS(
                @Serializable(with = ua.pp.lumivoid.util.OS.OsSerializer::class) val name: ua.pp.lumivoid.util.OS,
            )
        }
    }
}

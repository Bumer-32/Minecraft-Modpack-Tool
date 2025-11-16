package ua.pp.lumivoid.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ua.pp.lumivoid.util.OS

// https://piston-meta.mojang.com/v1/packages/a112787401cbfa9cafc848d81be2f87e3f760e3e/1.21.8.json

@Serializable
data class MinecraftJson(
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
        val rules: List<Rule>?
    ) {
        @Serializable
        data class Downloads(
            val artifact: Artifact,
            val name: String
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
            @Serializable(with = OS.OsSerializer::class) val os: OS
        )
    }
}

package ua.pp.lumivoid.data

import kotlinx.serialization.Serializable
import ua.pp.lumivoid.project.platform.Platforms

@Serializable
data class ProjectData(val project: Project, val minecraft: Minecraft) {
    @Serializable
    data class Project(
        val name: String,
        val author: String,
        val version: String,
    )

    @Serializable
    data class Minecraft(
        @Serializable(with = Platforms.PlatformsSerializer::class) val platform: Platforms,
        val minecraft: String,
        val loader: String,
        val optional: Optional? = null,
    ) {
        @Serializable
        data class Optional(
            val xmx: String? = null,
            val username: String? = null,
        )
    }
}
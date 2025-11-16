package ua.pp.lumivoid.data

import kotlinx.serialization.Serializable
import ua.pp.lumivoid.project.platform.Platforms

@Serializable
data class ProjectData(val project: Project) {
    @Serializable
    data class Project(
        val name: String,
        val author: String,
        val version: String,
        @Serializable(with = Platforms.PlatformsSerializer::class) val platform: Platforms,
        val minecraft: String,
        val loader: String,
    )
}
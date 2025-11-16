package ua.pp.lumivoid.project.platform

interface Platform {
    fun getGameVersions(): Map<String, Boolean>
    fun getLoaderVersions(): Map<String, Boolean>
    fun install(version: String, loaderVersion: String)
    fun check(): Boolean
    fun libraries()
}
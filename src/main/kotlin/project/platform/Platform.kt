package ua.pp.lumivoid.project.platform

import java.io.File

interface Platform {
    fun getGameVersions(): Map<String, Boolean>
    fun getLoaderVersions(): Map<String, Boolean>
    fun install(version: String, loaderVersion: String, path: File): Boolean
    fun launch(mcArgs: String, jvmArgs: String, version: String, loaderVersion: String, path: File)
    fun fastCheck(version: String, loaderVersion: String, path: File): Boolean
}
package ua.pp.lumivoid

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

object Constants {
    val httpClient = HttpClient(CIO)
}
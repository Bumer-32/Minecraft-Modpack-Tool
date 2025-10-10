package ua.pp.lumivoid.tasks

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "downloadGame",
    description = ["Downloads minecraft and libraries"]
)
object DownloadGame: Callable<Int> {
    override fun call(): Int {
        return 0
    }
}
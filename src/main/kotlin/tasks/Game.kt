package ua.pp.lumivoid.tasks

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "game",
    description = ["launching minecraft"]
)
object Game: Callable<Int> {
    override fun call(): Int {
        return 0
    }
}
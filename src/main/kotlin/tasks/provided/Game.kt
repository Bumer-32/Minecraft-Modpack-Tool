package ua.pp.lumivoid.tasks.provided

import ua.pp.lumivoid.tasks.Task

object Game: Task(
    "game",
    "launching minecraft"
) {
    override fun call(args: List<String>) {
    }
}
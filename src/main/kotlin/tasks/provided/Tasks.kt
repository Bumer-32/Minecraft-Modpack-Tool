package ua.pp.lumivoid.tasks.provided

import ua.pp.lumivoid.ArgsParser
import ua.pp.lumivoid.tasks.Task

object Tasks: Task(
    "tasks",
    "Shows the list of all tasks"
) {
    override fun call() {
        logger.info("Current provided tasks:")
        logger.info("-".repeat(40))
        ArgsParser.list().forEach { logger.info(it.represent()) }
        logger.info("-".repeat(40))
    }
}
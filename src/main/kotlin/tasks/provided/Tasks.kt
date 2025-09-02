package ua.pp.lumivoid.tasks.provided

import ua.pp.lumivoid.tasks.Task
import ua.pp.lumivoid.tasks.TasksParser

object Tasks: Task(
    "tasks",
    "Shows the list of all tasks"
) {
    override fun call(args: List<String>) {
        logger.info("Current provided tasks:")
        logger.info("-".repeat(40))
        TasksParser.list().forEach { logger.info(it.represent()) }
        logger.info("-".repeat(40))
    }
}
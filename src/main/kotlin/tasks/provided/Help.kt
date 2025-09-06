package ua.pp.lumivoid.tasks.provided

import ua.pp.lumivoid.tasks.Task
import ua.pp.lumivoid.tasks.TasksParser

object Help: Task(
    "help",
    "Shows help",
) {
    override fun call(args: List<String>) {
        val searchedTask = args.find { !it.startsWith("-") }
        if (searchedTask != null) {
            val task = TasksParser.list().find { it.name == searchedTask }

            if (task != null) {
                logger.info("Task: ${task.name} -> ${task.description}")
                if (task.taskArgs.isNotEmpty()) {
                    logger.info("Arguments:")
                    task.taskArgs.forEach { arg ->
                        logger.info("   ${arg.aliases.joinToString(" ")} - ${arg.description}")
                    }
                }
            } else {
                logger.error("searched task not found")
            }
        } else {
            logger.info("Usage: mmt [args] <task> [task args]")
            logger.info("E.g: mmt -d init --name hello")
            logger.info("")
            logger.info("To see all tasks: mmt tasks")
            logger.info("")
            logger.info("Additional help for each task: mmt help <task>")
        }
    }
}
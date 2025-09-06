package ua.pp.lumivoid.tasks.provided

import ua.pp.lumivoid.tasks.Task
import ua.pp.lumivoid.tasks.TasksParser

object Tasks: Task(
    "tasks",
    "Shows the list of all tasks"
) {
    override fun call(args: List<String>) {
        logger.info("Current provided tasks:")
        logger.info("")
        TasksParser.list().forEach { task ->
            var arguments = ""
            task.taskArgs.forEach { arg ->
                arg.aliases.forEach { alias ->
                    arguments += "$alias "
                }
            }

            logger.info("${task.name} ${if (arguments.isNotEmpty()) { "[${arguments}] " } else { "" }}-> ${task.description}")
        }
    }
}
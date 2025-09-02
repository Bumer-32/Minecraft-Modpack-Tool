package ua.pp.lumivoid.tasks

import org.slf4j.LoggerFactory
import ua.pp.lumivoid.ArgsParser.parseArgs

object TasksParser {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val tasks: MutableSet<TaskData> = mutableSetOf()


    fun registerTask(name: String, description: String, task: Task, args: List<String>) {
        tasks.add(TaskData(name, description, task, args))
    }

    fun processTask(args: Array<String>) {
        val parsedArgs = parseArgs(args).dropWhile { it.startsWith("-") }.drop(0 ) // if not starts with -, then it's for task

        logger.debug("Args for task: {}", parsedArgs)

        val task = tasks.find { it.name in args }
        if (task != null) logger.info("Task: " + task.name)
        task?.task?.call(parsedArgs)

        if (task == null) {
            logger.info("No one tasks was called")
        }
    }

    fun list(): Set<TaskData> = tasks
}

data class TaskData(val name: String, val description: String, val task: Task, val args: List<String>) {
    fun represent(): String {
        return "$name $args -> $description"
    }
}
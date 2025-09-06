package ua.pp.lumivoid.tasks

import org.slf4j.LoggerFactory
import ua.pp.lumivoid.ArgsParser.parseArgs
import ua.pp.lumivoid.tasks.provided.Help

object TasksParser {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val tasks: MutableList<Task> = mutableListOf()

    fun processTask(args: Array<String>) {
        val parsedArgs = parseArgs(args).dropWhile { it.startsWith("-") }.drop(1) // if not starts with -, then it's for task

        logger.debug("Args for task: {}", parsedArgs)

        val task = tasks.find { it.name in args }
        if (task != null) logger.info("Task: " + task.name)


        if (task == null) {
            logger.info("No one tasks was called, calling help")
            logger.info("-".repeat(15))
            Help.call(parsedArgs)
        } else {
            logger.info("-".repeat(15))
            task.call(parsedArgs)
        }

        logger.info("-".repeat(15))
    }

    fun registerTask(task: Task) = tasks.add(task)
    fun list(): List<Task> = tasks
}
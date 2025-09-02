package ua.pp.lumivoid.tasks

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Task(val name: String, val description: String = "No description provided", val taskArgs: List<TaskArgument> = emptyList()) {
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    abstract fun call(args: List<String>)

    fun register() {
        val args = mutableListOf<String>()

        taskArgs.forEach { arg ->
            arg.args.forEach { argArg ->
                args.add(argArg)
            }
        }

        TasksParser.registerTask(this.name, this.description, this, args.sorted())
    }

    /*
    null only possible when in your TaskArgument optional == true or noData = true
    */
    fun readArg(name: String, args: List<String>): String? {
        return taskArgs.find { it.name == name }?.read(args)
    }

    fun checkIsCorrect(): Boolean {
        logger.info("Is all information correct? [y/n]")
        val isCorrect = readln().lowercase()
        if (isCorrect  != "y" && isCorrect != "yes") {
            logger.info("Try again:")
            return false
        }
        return true
    }
}

/*
Creates argument for task, if it optional it will be avail only as cli argument (-a)
if it not optional user will be asked if cli argument not specified
No data needs for simple args without reading data like -y
 */
data class TaskArgument(val optional: Boolean,
                        val name: String,
                        val isNotEmpty: Boolean,
                        val args: List<String>,
                        val noData: Boolean = false,
                        val default: String? = null,
                        val check: (input: String) -> Boolean = { input ->
                            if (isNotEmpty) input.isNotEmpty() else true
                        },
) {

    fun read(inputArgs: List<String>): String? {
        args.forEach { arg ->
            val cliArg = inputArgs.withIndex().find {
                it.value == arg
            }

            if (noData) {
                if (cliArg != null) return cliArg.value
            } else {
                if (cliArg != null && check.invoke(inputArgs[cliArg.index + 1])) {
                    return inputArgs[cliArg.index + 1]
                } else if (cliArg == null && optional) {
                    return default
                }
            }
        }

        if (noData) return null

        var value: String
        do {
            print("$name${if (default == null) "" else " [$default]"}: ")
            value = readln()
        } while (!check.invoke(value))

        if (value.trim().isEmpty() && default != null) value = default

        return value
    }
}
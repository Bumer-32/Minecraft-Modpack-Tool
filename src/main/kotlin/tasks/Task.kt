package ua.pp.lumivoid.tasks

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Task(val name: String, val description: String = "No description provided", val taskArgs: List<TaskArgument> = emptyList()) {
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    abstract fun call(args: List<String>)

    fun register() {
        TasksParser.registerTask(this)
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
    val name: String,
    val description: String,
    val required: Boolean, - is user will be asked if cli argument not specialised
    val isNotEmpty: Boolean, - can be ""?
    val hasValue: Boolean, - --yes -> no value, --name myname -> value
    val aliases: List<String>,
    val default: String? = null,
    val validator: (input: String) -> Boolean = { input ->
        if (isNotEmpty) input.isNotEmpty() else true
    },
 */
data class TaskArgument(
    val name: String,
    val description: String,
    val required: Boolean,
    val isNotEmpty: Boolean? = null,
    val hasValue: Boolean,
    val aliases: List<String>,
    val default: String? = null,
    val validator: (input: String) -> Boolean = { input ->
        if (isNotEmpty!!) input.isNotEmpty() else true
    },
) {

    fun read(inputArgs: List<String>): String? {
        aliases.forEach { arg ->
            val cliArg = inputArgs.withIndex().find {
                it.value == arg
            }

            if (hasValue) {
                if (cliArg != null && validator.invoke(inputArgs[cliArg.index + 1])) {
                    return inputArgs[cliArg.index + 1]
                } else if (cliArg == null && !required) {
                    return default
                }
            } else {
                if (cliArg != null) return cliArg.value
            }
        }

        if (!hasValue) return null

        var value: String
        do {
            print("$name${if (default == null) "" else " [$default]"}: ")
            value = readln()
        } while (!validator.invoke(value))

        if (value.trim().isEmpty() && default != null) value = default

        return value
    }
}
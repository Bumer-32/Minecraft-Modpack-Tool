package ua.pp.lumivoid

import ua.pp.lumivoid.tasks.Task

object ArgsParser {
    private val tasks: MutableSet<TaskData> = mutableSetOf()

    fun registerTask(name: String, description: String, task: Task) {
        tasks.add(TaskData(name, description, task))
    }

    /*
    * For now, it's parsing only tasks
    */
    fun parse(args: Array<String>) {
        val task = tasks.find { it.name == args[0] }
        task?.task?.call()

    }

    fun list(): Set<TaskData> = tasks
}

data class TaskData(val name: String, val description: String, val task: Task) {
    fun represent(): String {
        return "$name -> $description"
    }
}
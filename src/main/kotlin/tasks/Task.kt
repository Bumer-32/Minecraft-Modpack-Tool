package ua.pp.lumivoid.tasks

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.ArgsParser

abstract class Task(val name: String, val description: String = "No description provided") {
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    abstract fun call()

    fun register() {
        ArgsParser.registerTask(this.name, this.description, this)
    }
}
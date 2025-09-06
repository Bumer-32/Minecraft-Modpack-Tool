package ua.pp.lumivoid

import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import ua.pp.lumivoid.tasks.TasksParser
import ua.pp.lumivoid.tasks.provided.Help
import ua.pp.lumivoid.tasks.provided.Init
import ua.pp.lumivoid.tasks.provided.Tasks
import java.util.logging.Level
import java.util.logging.Logger


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    ArgsParser.processArgs(args)

    val logger = LoggerFactory.getLogger("Main")

    logger.info("Starting Minecraft Modpack Tool!")

    // redirect java logging
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    //jline logger
    Logger.getLogger("org.jline").level = Level.FINE

    Help.register() // important to register first
    Tasks.register()
    Init.register()

    TasksParser.processTask(args)

    logger.info("EOW!")
}

var isQuietMode = false
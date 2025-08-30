package ua.pp.lumivoid

import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import ua.pp.lumivoid.tasks.provided.Init
import ua.pp.lumivoid.tasks.provided.Tasks
import java.util.logging.Level
import java.util.logging.Logger


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    // debug logger
    if (args.contains("--debug")) {
        System.setProperty("debug", "true")
    }

    val logger = LoggerFactory.getLogger("Main")

    logger.info("Starting Minecraft Modpack Tool!")

    // redirect java logging
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    //jline logger
    Logger.getLogger("org.jlinee").level = Level.FINE

    Tasks.register()
    Init.register()


    ArgsParser.parse(args)

    logger.info("EOW!")
}
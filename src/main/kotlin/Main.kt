package ua.pp.lumivoid

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import picocli.CommandLine
import ua.pp.lumivoid.tasks.DownloadGame
import ua.pp.lumivoid.tasks.Game
import ua.pp.lumivoid.tasks.Init
import java.util.Properties
import java.util.concurrent.Callable
import kotlin.jvm.javaClass
import kotlin.system.exitProcess


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val cmd = CommandLine(Main)

    cmd.executionStrategy = CommandLine.IExecutionStrategy { parseResult ->
        val main = cmd.commandSpec.commandLine().getCommand<Main>()

        if(main.quiet) System.setProperty("quiet", "true")
        if(main.debug) System.setProperty("debug", "true")

        // reload configuration
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context
        context.reset()
        val configStream = Thread.currentThread().contextClassLoader.getResourceAsStream("logback.xml")
        configurator.doConfigure(configStream)

        // and then start logging
        val logger = LoggerFactory.getLogger("mmt")

        logger.info("Starting Minecraft Modpack Tool!")
        logger.debug("DEBUG")

        // redirect java logging
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        CommandLine.RunAll().execute(parseResult)
    }

    val exitCode = cmd.execute(*args)
    LoggerFactory.getLogger("mmt").info("EOW!")
    exitProcess(exitCode)
}

class VersionProvider: CommandLine.IVersionProvider {
    override fun getVersion(): Array<String> {
        val props = Properties()
        props.load(javaClass.getResourceAsStream("/version.properties"))
        val version = props.getProperty("version", "unknown")
        return arrayOf(version)
    }
}

@CommandLine.Command(
    name = "mmt",
    description = ["Minecraft Modpack Tool"],
    subcommands = [
        Init::class,
        Game::class,
        DownloadGame::class,
    ],
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider::class
)
object Main: Callable<Int> {

    @CommandLine.Option(names = ["-q", "--quiet"], description = ["Disables any output from mmt"])
    var quiet = false

    @CommandLine.Option(names = ["-d", "--debug"], description = ["Enables debug logging"])
    var debug = false

    override fun call(): Int {
        return 0
    }
}

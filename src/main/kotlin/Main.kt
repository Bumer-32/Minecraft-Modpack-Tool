package ua.pp.lumivoid

import org.slf4j.LoggerFactory


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Main")

    logger.info("Hello world!")

    args.forEach {
        logger.info(it)
    }
}
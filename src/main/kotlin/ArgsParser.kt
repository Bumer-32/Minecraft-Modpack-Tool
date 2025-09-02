package ua.pp.lumivoid

import kotlin.collections.forEach
import kotlin.text.forEach

object ArgsParser {
    // ! important NOT TO INITIALIZE LOGGER HERE

    /*
    processing args for program, not for tasks
    */
    fun processArgs(args: Array<String>) {
        val parsedArgs = parseArgs(args).takeWhile { it.startsWith("-") } // if not starts with -, then it's for task

        parsedArgs.forEach { arg ->
            ArgsFunctions.entries.forEach inner@ { entry ->
                if (arg in entry.args) {
                    entry.call()
                    return@inner
                }
            }
        }
    }

    fun parseArgs(args: Array<String>): List<String> {
        val parsedArgs = mutableListOf<String>()

        args.forEach { arg ->

            if (arg.startsWith("-") && arg[1] != '-') {
                if (arg.length > 2) {
                    arg.forEach {
                        parsedArgs.add("-$it") // -q -p -t -f instead of -qptf
                    }
                }
                else parsedArgs.add(arg)
            }
            else parsedArgs.add(arg)
        }

        return parsedArgs
    }


    private enum class ArgsFunctions(vararg val args: String) {
        QuietMode("-q", "--quiet") {
            override fun call() {
                isQuietMode = true
                System.setProperty("quiet", "true")
            }
        },
        Debug("-d", "--debug") {
            override fun call() {
                System.setProperty("debug", "true")
            }
        },
        ;

        abstract fun call()
    }
}
package ua.pp.lumivoid.util

import java.io.File

object CheckInput {
    fun checkStr(name: String, str: String?): String {
        if (str == null) {
            print("$name: ")
            return readln()
        }

        return str
    }

    fun checkFilledStr(name: String, str: String?): String {
        if (str == null || str.isEmpty()) {
            while (true) {
                print("$name: ")
                val read = readln()
                if (read.isNotEmpty()) return read
            }
        }

        return str
    }

    fun checkInt(name: String, int: Int?): Int {
        if (int == null) {
            while (true) {
                print("$name: ")
                runCatching { return readln().toInt() }
            }
        }

        return int
    }

    fun checkFile(name: String, file: File?): File {
        if (file == null) {
            while (true) {
                print("$name: ")
                runCatching { return File(readln().trim()) }
            }
        }

        return file
    }

    fun checkChoosable(name: String, variants: List<String>, choose: String?): String {
        if (choose != null && choose in variants) return choose

        while (true) {
            println("$name: ")
            variants.forEachIndexed { index, variant ->
                println(" ${index + 1} - $variant")
            }

            print("Your choice: ")
            runCatching {
                val choice = readln().toInt()
                return variants[choice - 1]
            }
        }
    }

    fun confirm(name: String = "Are you sure?"): Boolean {
        print("$name [y/n]: ")
        return readln().lowercase() in listOf("yes", "y", "1", "true")
    }
}
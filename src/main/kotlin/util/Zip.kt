package ua.pp.lumivoid.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object Zip {
    fun zipFolder(dest: File) {
        ZipOutputStream(FileOutputStream(dest)).use { zos ->
            fun addFile(file: File, parentPath: String) {
                val entry = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
                if (file.isDirectory) {
                    if (file.list()?.isEmpty() != false) {
                        zos.putNextEntry(ZipEntry(entry))
                        zos.closeEntry()
                    }
                    file.listFiles()?.forEach { addFile(it, entry) }
                } else {
                    FileInputStream(file).use { fis ->
                        zos.putNextEntry(ZipEntry(entry))
                        fis.copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }
        }
    }

    fun unzipToFolder(source: File, dest: File) {
        ZipInputStream(FileInputStream(source)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val outFile = File(dest, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}
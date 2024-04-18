package org.usvm.utils

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object FileUtils {
    val tvmTempDirectory: File
        get() = File(tempDirectoryPath).resolve(tvmFolderName).also {
            it.mkdirs()
        }

    private val tempDirectoryPath = System.getProperty("java.io.tmpdir")
    private const val tvmFolderName = "TvmTsa"

    fun extractZipFromResource(zipResourcePath: String, outputDir: File) {
        ZipInputStream(
            FileUtils::class.java.classLoader.getResourceAsStream(zipResourcePath) ?:
                error("$zipResourcePath not found on the classpath")
        ).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                val newFile = File(outputDir, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile.mkdirs()
                    FileOutputStream(newFile).use { zipStream.copyTo(it) }
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
        }
    }
}

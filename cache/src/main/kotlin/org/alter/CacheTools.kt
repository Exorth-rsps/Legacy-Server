package org.alter

import dev.openrune.cache.tools.Builder
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.cache.tools.tasks.TaskType
import dev.openrune.tools.PackServerConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

fun getCacheLocation() = File("../data/","cache").path
fun getRawCacheLocation(dir : String) = File("../data/","raw-cache/$dir/")

private val logger = KotlinLogging.logger {}

fun main(args : Array<String>) {
    if (args.isEmpty()) {
        println("Usage: <buildType>")
        exitProcess(1)
    }
    downloadRev(TaskType.valueOf(args.first().uppercase()))
}

fun downloadRev(type : TaskType) {

    val rev = readRevision()

    logger.error { "Using Revision: $rev" }

    val tasks : MutableList<CacheTask> = listOf(
        PackServerConfig()
    ).toMutableList()

    when(type) {
        TaskType.FRESH_INSTALL -> {
            val builder = Builder(type = TaskType.FRESH_INSTALL, File(getCacheLocation()))
            builder.revision(rev)
            builder.removeXteas()
            builder.removeBzip()
            builder.extraTasks(*tasks.toTypedArray()).build().initialize()

            Files.move(
                File(getCacheLocation(), "xteas.json").toPath(),
                File("../data/","xteas.json").toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
        TaskType.BUILD -> {
            val builder = Builder(type = TaskType.BUILD, cacheLocation = File(getCacheLocation()))
            builder.revision(rev)
            builder.extraTasks(*tasks.toTypedArray()).build().initialize()
        }
    }


}

fun readRevision(): Int {
    val file = listOf("../game.yml", "../game.example.yml")
        .map(::File)
        .first { it.exists() } // guaranteed one exists

    return file.useLines { lines ->
        lines.first { it.trim().startsWith("revision:") }
            .substringAfter("revision:")
            .trim()
            .substringBefore('.')
            .toInt()
    }
}

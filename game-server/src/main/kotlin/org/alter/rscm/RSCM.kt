package org.alter.rscm

import dev.openrune.definition.constants.ConstantProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

/**
 * @author Cl0udS3c
 */
object RSCM {

    fun getRSCM(entity: Array<String>): List<Int> = entity.map { getRSCM(it) }.toList()


    fun getRSCM(entity: String) : Int = ConstantProvider.getMapping(entity)



}
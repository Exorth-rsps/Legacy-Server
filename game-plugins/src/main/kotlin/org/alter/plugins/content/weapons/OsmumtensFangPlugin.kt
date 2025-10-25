package org.alter.plugins.content.weapons

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*

import org.alter.game.plugin.*


/**
 * @author CloudS3c 12/29/2024
 */
class OsmumtensFangPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        setItemCombatLogic("item.osmumtens_fang") {
            val attackStyle = player.getAttackStyle()
            //9471 1-2 attacks
        }
    }
}

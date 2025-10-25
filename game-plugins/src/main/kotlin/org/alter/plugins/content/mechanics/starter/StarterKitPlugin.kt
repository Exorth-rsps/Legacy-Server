package org.alter.plugins.content.mechanics.starter

import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.NEW_ACCOUNT_ATTR
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM

class StarterKitPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        onLogin {
            if (player.attr[NEW_ACCOUNT_ATTR] ?: return@onLogin) {
                with(player.inventory) {
                    add(getRSCM("item.logs"), 5)
                    add(getRSCM("item.tinderbox"))
                    add(getRSCM("item.bread"), 5)
                    add(getRSCM("item.bronze_pickaxe"))
                    add(getRSCM("item.bronze_dagger"))
                    add(getRSCM("item.knife"))
                }
            }
        }

    }
}

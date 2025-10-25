package org.alter.plugins.content.interfaces.itemsets

import dev.openrune.ServerCacheManager.getEnumOrDefault
import dev.openrune.ServerCacheManager.getItemOrDefault
import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.attr.INTERACTING_ITEM_ID
import org.alter.game.model.attr.INTERACTING_OPT_ATTR
import org.alter.game.model.attr.INTERACTING_SLOT_ATTR
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.priv.Privilege
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*
import org.alter.plugins.content.interfaces.itemsets.ItemSets
import org.alter.plugins.content.interfaces.itemsets.ItemSets.openSets

class ItemsetsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

}

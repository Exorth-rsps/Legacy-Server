package dev.openrune.tools

import com.displee.cache.index.archive.file.File
import dev.openrune.OsrsCacheProvider
import dev.openrune.cache.CONFIGS
import dev.openrune.cache.CacheManager
import dev.openrune.cache.tools.TaskPriority
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.codec.osrs.impl.HealthBarServerCodec
import dev.openrune.codec.osrs.impl.ItemServerCodec
import dev.openrune.codec.osrs.impl.NpcServerCodec
import dev.openrune.codec.osrs.impl.ObjectServerCodec
import dev.openrune.codec.osrs.impl.SequenceServerCodec
import dev.openrune.definition.*
import dev.openrune.definition.codec.*
import dev.openrune.filesystem.Cache
import dev.openrune.server.impl.item.ItemRenderDataManager
import dev.openrune.server.infobox.InfoBoxItem
import dev.openrune.server.infobox.InfoBoxObject
import dev.openrune.server.infobox.Load
import dev.openrune.types.HealthBarServerType
import dev.openrune.types.ItemServerType
import dev.openrune.types.NpcServerType
import dev.openrune.types.ObjectServerType
import dev.openrune.types.SequenceServerType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.alter.getRawCacheLocation
import kotlin.io.path.Path


class PackServerConfig : CacheTask() {

    val logger = KotlinLogging.logger {}

    override val priority: TaskPriority
        get() = TaskPriority.END

    override fun init(cache: Cache) {
        logger.info { "Packing server configurations..." }

        CacheManager.init(OsrsCacheProvider(cache,revision))

        ItemRenderDataManager.init()

        val codec = ObjectServerCodec(
            CacheManager.getObjects(),
            InfoBoxObject.load(getRawCacheLocation("extra-dump").toPath().resolve("object-examines.csv"))
        )
        val codec2 = HealthBarServerCodec(CacheManager.getHealthBars())
        val codec3 = SequenceServerCodec(CacheManager.getAnims())
        val codec4 = NpcServerCodec(CacheManager.getNpcs())
        val codec5 = ItemServerCodec(
            CacheManager.getItems(),
            CacheManager.getEnums(),
            InfoBoxItem.load(    getRawCacheLocation("extra-dump").toPath().resolve("items.json")))

        logger.info { "Packing Objects..." }
        CacheManager.getObjects().forEach {
            cache.write(CONFIGS, 55, it.key, codec.encodeToBuffer(ObjectServerType(it.key)))
        }

        logger.info { "Packing Health Bars..." }
        CacheManager.getHealthBars().forEach {
            cache.write(CONFIGS, 56, it.key, codec2.encodeToBuffer(HealthBarServerType(it.key)))
        }

        logger.info { "Packing Animations..." }
        CacheManager.getAnims().forEach {
            cache.write(CONFIGS, 57, it.key, codec3.encodeToBuffer(SequenceServerType(it.key)))
        }

        logger.info { "Packing NPCs..." }
        CacheManager.getNpcs().forEach {
            cache.write(CONFIGS, 58, it.key, codec4.encodeToBuffer(NpcServerType(it.key)))
        }

        logger.info { "Packing Items..." }
        CacheManager.getItems().forEach {
            cache.write(CONFIGS, 59, it.key, codec5.encodeToBuffer(ItemServerType(it.key)))
        }

        logger.info { "Finished packing all server configurations." }
    }
}

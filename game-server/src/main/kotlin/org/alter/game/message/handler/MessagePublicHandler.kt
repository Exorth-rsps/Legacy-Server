package org.alter.game.message.handler

import net.rsprot.protocol.game.incoming.messaging.MessagePublic
import org.alter.game.info.PlayerInfo
import org.alter.game.message.MessageHandler
import org.alter.game.model.entity.Client
import org.alter.game.service.log.LoggerService

/**
 * @author Tom <rspsmods@gmail.com>
 */
class MessagePublicHandler : MessageHandler<MessagePublic> {
    override fun consume(
        client: Client,
        message: MessagePublic,
    ) {
        PlayerInfo(client).setChat(
            colour = message.colour,
            effect = message.effect,
            icon = client.privilege.icon,
            auto = message.type == 1,
            message = message.message,
            pattern = message.pattern?.asByteArray(),
        )
        client.world.getService(LoggerService::class.java, searchSubclasses = true)?.logPublicChat(client, message.message)
    }
}

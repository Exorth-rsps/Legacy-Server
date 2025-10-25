package org.alter.game.message.handler

import net.rsprot.protocol.game.incoming.events.EventMouseClickV2
import org.alter.game.message.MessageHandler
import org.alter.game.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventMouseClickHandler : MessageHandler<EventMouseClickV2> {
    override fun consume(
        client: Client,
        message: EventMouseClickV2,
    ) {
        /**
         * TODO("Implement mouse event click")
         */
    }
}

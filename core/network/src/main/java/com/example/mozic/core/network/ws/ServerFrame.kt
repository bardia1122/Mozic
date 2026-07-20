package com.example.mozic.core.network.ws

import com.example.mozic.core.domain.model.chat.Message

/** Decoded server -> client WS frames (`backend/PROTOCOL.md`), already mapped to domain [Message]. */
sealed interface ServerFrame {
    data class Ack(val messageId: String) : ServerFrame

    data class Push(val message: Message) : ServerFrame

    /** The peer read up to [upToMs] — i.e. *my own* sent messages up to that time just became `READ`. */
    data class Read(val conversationId: String, val upToMs: Long) : ServerFrame

    data class Typing(val conversationId: String, val isTyping: Boolean) : ServerFrame
}

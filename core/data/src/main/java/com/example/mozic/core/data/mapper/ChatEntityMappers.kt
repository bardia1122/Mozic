package com.example.mozic.core.data.mapper

import com.example.mozic.core.data.local.entity.ConversationEntity
import com.example.mozic.core.data.local.entity.MessageEntity
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.model.chat.MessagePayload
import com.example.mozic.core.domain.model.chat.MessageStatus

private const val PAYLOAD_TYPE_SONG = "song"

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    sentAtEpochMs = sentAtEpochMs,
    status = MessageStatus.valueOf(status),
    payload = if (payloadType == PAYLOAD_TYPE_SONG) {
        MessagePayload.SongShare(
            songId = songId.orEmpty(),
            title = songTitle.orEmpty(),
            artistName = songArtist.orEmpty(),
            coverImageUrl = songCover.orEmpty(),
        )
    } else {
        MessagePayload.Text(text.orEmpty())
    },
)

fun Message.toEntity(): MessageEntity {
    val payloadType = if (payload is MessagePayload.SongShare) PAYLOAD_TYPE_SONG else "text"
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        sentAtEpochMs = sentAtEpochMs,
        status = status.name,
        payloadType = payloadType,
        text = (payload as? MessagePayload.Text)?.text,
        songId = (payload as? MessagePayload.SongShare)?.songId,
        songTitle = (payload as? MessagePayload.SongShare)?.title,
        songArtist = (payload as? MessagePayload.SongShare)?.artistName,
        songCover = (payload as? MessagePayload.SongShare)?.coverImageUrl,
    )
}

fun ConversationEntity.toDomain(lastMessage: Message?, unreadCount: Int): Conversation = Conversation(
    id = id,
    peer = User(
        id = peerId,
        username = peerUsername,
        displayName = peerDisplayName,
        avatarUrl = peerAvatarUrl,
        isPremium = peerIsPremium,
        // Follow state is C6's (social graph, not yet built) — unknown here.
        isFollowed = false,
    ),
    lastMessage = lastMessage,
    unreadCount = maxOf(unreadCount, if (forcedUnread) 1 else 0),
)

fun Conversation.toEntity(): ConversationEntity = ConversationEntity(
    id = id,
    peerId = peer.id,
    peerUsername = peer.username,
    peerDisplayName = peer.displayName,
    peerAvatarUrl = peer.avatarUrl,
    peerIsPremium = peer.isPremium,
)

package com.example.mozic.core.data.fake

import com.example.mozic.core.domain.model.Artist
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.model.chat.MessagePayload
import com.example.mozic.core.domain.model.chat.MessageStatus

/**
 * Canned data that backs every fake repository so screens can be built and
 * demoed before the real Room/Ktor/ExoPlayer implementations exist. Cover art
 * is served by picsum.photos and audio by SoundHelix — both stable public URLs.
 */
object SampleData {
    const val CURRENT_USER_ID = "me"

    val artists = listOf(
        sampleArtist(1, "Nova Cascade", followers = 128_400),
        sampleArtist(2, "The Midnight Echo", followers = 512_900),
        sampleArtist(3, "Solaris", followers = 84_120),
        sampleArtist(4, "Kavana", followers = 1_204_000),
        sampleArtist(5, "Lo-Fi Collective", followers = 47_800),
    )

    val songs = listOf(
        sampleSong(1, "Neon Skyline", "Nova Cascade", 214_000, liked = true),
        sampleSong(2, "Slow Tide", "The Midnight Echo", 198_500),
        sampleSong(3, "Paper Planes", "Solaris", 176_000, liked = true),
        sampleSong(4, "Gold Hour", "Kavana", 231_200),
        sampleSong(5, "Static Bloom", "Nova Cascade", 205_800),
        sampleSong(6, "Rooftops", "The Midnight Echo", 189_300),
        sampleSong(7, "Undertow", "Solaris", 242_100),
        sampleSong(8, "Midnight Drive", "Kavana", 217_600, liked = true),
        sampleSong(9, "Coffee & Rain", "Lo-Fi Collective", 163_400),
        sampleSong(10, "Afterglow", "Lo-Fi Collective", 172_900),
    )

    val playlists = listOf(
        samplePlaylist(1, "Global Top 50", PlaylistCategory.WORLD, songCount = 50),
        samplePlaylist(2, "Fresh Finds", PlaylistCategory.WORLD, songCount = 40),
        samplePlaylist(3, "Tehran Nights", PlaylistCategory.LOCAL, songCount = 32),
        samplePlaylist(4, "Local Heroes", PlaylistCategory.LOCAL, songCount = 28),
        samplePlaylist(5, "My Focus Mix", PlaylistCategory.USER, songCount = 18, ownerId = "u1"),
        samplePlaylist(6, "Late Night", PlaylistCategory.USER, songCount = 12, ownerId = "u2"),
    )

    val users = listOf(
        sampleUser(1, "sara.m", "Sara Moradi", premium = true, followed = true),
        sampleUser(2, "arman", "Arman K.", followed = true),
        sampleUser(3, "lily", "Lily Chen"),
        sampleUser(4, "dj_reza", "DJ Reza", premium = true),
        sampleUser(5, "noor", "Noor A."),
    )

    val conversations = listOf(
        Conversation(id = "c1", peer = users[0], lastMessage = null, unreadCount = 2),
        Conversation(id = "c2", peer = users[1], lastMessage = null, unreadCount = 0),
    )

    val messagesByConversation: Map<String, List<Message>> = mapOf(
        "c1" to listOf(
            textMessage("m1", "c1", "u1", offsetMs = 0, MessageStatus.READ, "hey! found something for you"),
            songShareMessage("m2", "c1", "u1", offsetMs = 60_000, songs[2]),
            textMessage("m3", "c1", CURRENT_USER_ID, offsetMs = 120_000, MessageStatus.SENT, "on repeat already 🔥"),
        ),
        "c2" to listOf(
            textMessage("m4", "c2", CURRENT_USER_ID, offsetMs = 0, MessageStatus.READ, "playlist for the drive?"),
            songShareMessage("m5", "c2", "u2", offsetMs = 45_000, songs[7]),
        ),
    )

    private fun sampleArtist(n: Int, name: String, followers: Int) = Artist(
        id = "a$n",
        name = name,
        imageUrl = "https://picsum.photos/seed/mozic-a$n/300/300",
        followerCount = followers,
    )

    private fun sampleSong(
        n: Int,
        title: String,
        artist: String,
        durationMs: Long,
        liked: Boolean = false,
    ) = Song(
        id = "s$n",
        title = title,
        artistName = artist,
        coverImageUrl = "https://picsum.photos/seed/mozic-s$n/400/400",
        audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$n.mp3",
        durationMs = durationMs,
        isLiked = liked,
    )

    private fun samplePlaylist(
        n: Int,
        title: String,
        category: PlaylistCategory,
        songCount: Int,
        ownerId: String? = null,
        isPublic: Boolean = true,
    ) = Playlist(
        id = "p$n",
        title = title,
        coverImageUrl = "https://picsum.photos/seed/mozic-p$n/400/400",
        ownerId = ownerId,
        isPublic = isPublic,
        category = category,
        songCount = songCount,
    )

    private fun sampleUser(
        n: Int,
        username: String,
        displayName: String,
        premium: Boolean = false,
        followed: Boolean = false,
    ) = User(
        id = "u$n",
        username = username,
        displayName = displayName,
        avatarUrl = "https://picsum.photos/seed/mozic-u$n/200/200",
        isPremium = premium,
        isFollowed = followed,
    )

    private fun textMessage(
        id: String,
        conversationId: String,
        senderId: String,
        offsetMs: Long,
        status: MessageStatus,
        text: String,
    ) = Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        sentAtEpochMs = BASE_TIME + offsetMs,
        status = status,
        payload = MessagePayload.Text(text),
    )

    private fun songShareMessage(
        id: String,
        conversationId: String,
        senderId: String,
        offsetMs: Long,
        song: Song,
    ) = Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        sentAtEpochMs = BASE_TIME + offsetMs,
        status = MessageStatus.READ,
        payload = MessagePayload.SongShare(
            songId = song.id,
            title = song.title,
            artistName = song.artistName,
            coverImageUrl = song.coverImageUrl,
        ),
    )

    private const val BASE_TIME = 1_720_000_000_000L
}

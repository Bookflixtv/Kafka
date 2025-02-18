package com.kafka.data.entities

import com.google.firebase.firestore.DocumentId
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class FavoriteItem(
    @DocumentId
    @Transient val itemId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("creator") val creator: String = "",
    @SerialName("mediaType") val mediaType: String = "",
    @SerialName("coverImage") val coverImage: String = "",
    @SerialName("createdAt") val createdAt: Timestamp = Timestamp.now(),
)

fun FavoriteItem.toItem() = Item(
    itemId = itemId,
    title = title,
    creator = Creator(id = creator, name = creator),
    mediaType = mediaType,
    coverImage = coverImage,
)

const val listIdFavorites = "items"

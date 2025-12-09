package de.rogallab.mobile.data.local.dtos

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rogallab.mobile.domain.utilities.newUuid

@Entity(tableName = "Article")
data class ArticleRoomDto(
   @PrimaryKey
   val id: String = newUuid(),
   val author: String? = "",
   val title: String? = "",
   val description: String? = "",
   val url: String? = "",
   val urlToImage: String? = "",
   val publishedAt: String? = "",
   val sourceName: String? = "",
)
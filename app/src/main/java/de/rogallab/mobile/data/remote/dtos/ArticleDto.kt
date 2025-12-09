package de.rogallab.mobile.data.remote.dtos

import androidx.room.Entity
import de.rogallab.mobile.domain.utilities.newUuid
import kotlinx.serialization.Serializable

@Serializable
data class ArticleDto(
   val id: String = newUuid(),
   val author: String? = "",
   val title: String? = "",
   val description: String? = "",
   val url: String? = "",
   val urlToImage: String? = "",
   val publishedAt: String? = "",
   val content: String? = "",
   val source: SourceDto? = SourceDto(),
)
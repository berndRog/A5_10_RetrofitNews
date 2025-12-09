package de.rogallab.mobile.domain.entites

import de.rogallab.mobile.domain.utilities.newUuid

data class Article(
   val author: String?,
   val title: String,
   val description: String,
   val url: String,
   val urlToImage: String,
   val publishedAt: String,        // or LocalDateTime later
   val sourceName: String?,
   val id: String = newUuid()
)
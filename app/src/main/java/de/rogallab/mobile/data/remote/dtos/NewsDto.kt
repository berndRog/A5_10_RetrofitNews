package de.rogallab.mobile.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class NewsDto(
   val status: String = "",
   val totalResults: Int = 0,
   val articles: List<ArticleDto> = listOf(),
)
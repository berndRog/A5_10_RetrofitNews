package de.rogallab.mobile.data.models

data class News(
   val articles: List<Article> = listOf(),
   val status: String = "",
   val totalResults: Int = 0
)
package de.rogallab.mobile.test.data

import de.rogallab.mobile.data.local.dtos.ArticleRoomDto
import de.rogallab.mobile.data.mappings.toArticle
import de.rogallab.mobile.data.mappings.toArticleRoomDto
import de.rogallab.mobile.data.remote.dtos.ArticleDto
import de.rogallab.mobile.data.remote.dtos.NewsDto
import de.rogallab.mobile.domain.entites.Article
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent

class SeedTestdata(): KoinComponent {

   private val json = Json {
      ignoreUnknownKeys = true
      isLenient = true
      prettyPrint = true
   }

   val jsonListOfArticles = """
      {
        "status": "ok",
        "totalResults": 3,
        "articles": [
          {
            "title": "Test title 1",
            "description": "Test description 1",
            "url": "https://example.com/article1",
            "urlToImage": "https://example.com/image1.jpg",
            "publishedAt": "2025-12-10T08:00:00Z"
          },
          {
            "title": "Test title 2",
            "description": "Test description 3",
            "url": "https://example.com/article2",
            "urlToImage": "https://example.com/image3.jpg",
            "publishedAt": "2025-12-10T09:00:00Z"
          },
          {
            "title": "Test title 3",
            "description": "Test description 3",
            "url": "https://example.com/article3",
            "urlToImage": "https://example.com/image3.jpg",
            "publishedAt": "2025-12-10T10:00:00Z"
          }
        ]
      }
      """.trimIndent()

   val newsDto: NewsDto = json.decodeFromString<NewsDto>(jsonListOfArticles)
   val articleDtos:     List<ArticleDto>     = newsDto.articles
   val articles:        List<Article>        = articleDtos.map { it.toArticle() }
   val articleRoomDtos: List<ArticleRoomDto> = articles.map { it.toArticleRoomDto() }

}
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

   // -------------------- Seed JSON (paged responses) --------------------
   fun newsPage(page:Int): String = when(page) {
      1 ->
         """
         {
           "status": "ok",
           "totalResults": 11,
           "articles": [
             { "title": "Test title 1", "description": "Test description 1", "url": "https://example.com/article1", "urlToImage": "https://example.com/image1.jpg", "publishedAt": "2025-12-10T08:00:00Z" },
             { "title": "Test title 2", "description": "Test description 2", "url": "https://example.com/article2", "urlToImage": "https://example.com/image2.jpg", "publishedAt": "2025-12-10T09:00:00Z" },
             { "title": "Test title 3", "description": "Test description 3", "url": "https://example.com/article3", "urlToImage": "https://example.com/image3.jpg", "publishedAt": "2025-12-10T10:00:00Z" },
             { "title": "Test title 4", "description": "Test description 4", "url": "https://example.com/article4", "urlToImage": "https://example.com/image4.jpg", "publishedAt": "2025-12-10T11:00:00Z" },
             { "title": "Test title 5", "description": "Test description 5", "url": "https://example.com/article5", "urlToImage": "https://example.com/image5.jpg", "publishedAt": "2025-12-10T12:00:00Z" }
           ]
         } 
         """.trimIndent()
      2 ->
         """
         {
           "status": "ok",
           "totalResults": 11,
           "articles": [
             { "title": "Test title 6", "description": "Test description 6", "url": "https://example.com/article6", "urlToImage": "https://example.com/image6.jpg", "publishedAt": "2025-12-10T13:00:00Z" },
             { "title": "Test title 7", "description": "Test description 7", "url": "https://example.com/article7", "urlToImage": "https://example.com/image7.jpg", "publishedAt": "2025-12-10T14:00:00Z" },
             { "title": "Test title 8", "description": "Test description 8", "url": "https://example.com/article8", "urlToImage": "https://example.com/image8.jpg", "publishedAt": "2025-12-12T15:00:00Z" },
             { "title": "Test title 9", "description": "Test description 9", "url": "https://example.com/article9", "urlToImage": "https://example.com/image9.jpg", "publishedAt": "2025-12-10T16:00:00Z" },
             { "title": "Test title 10", "description": "Test description 10", "url": "https://example.com/article10", "urlToImage": "https://example.com/image10.jpg", "publishedAt": "2025-12-11T17:00:00Z" }
           ]
         }
         """.trimIndent()
      3 ->
         """
         {
           "status": "ok",
           "totalResults": 11,
           "articles": [
             { "title": "Test title 11", "description": "Test description 11", "url": "https://example.com/article11", "urlToImage": "https://example.com/image11.jpg", "publishedAt": "2025-12-12T18:00:00Z" }
           ]
         }
         """.trimIndent()
      else ->
         """
         {
            "status": "ok",
            "totalResults": 11,
            "articles": []
         }
         """.trimIndent()
   }

   val newsDto: NewsDto = json.decodeFromString<NewsDto>(jsonListOfArticles)
   val articleDtos:     List<ArticleDto>     = newsDto.articles
   val articles:        List<Article>        = articleDtos.map { it.toArticle() }
   val articleRoomDtos: List<ArticleRoomDto> = articles.map { it.toArticleRoomDto() }

}
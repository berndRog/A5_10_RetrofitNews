package de.rogallab.mobile.data.mappings

import de.rogallab.mobile.data.local.dtos.ArticleRoomDto
import de.rogallab.mobile.data.remote.dtos.ArticleDto
import de.rogallab.mobile.data.remote.dtos.SourceDto
import de.rogallab.mobile.domain.entites.Article

// ---------------------------------------------------------------------
// Article: Remote DTO ⇄ Domain
// ---------------------------------------------------------------------
// DTO -> Domain
fun ArticleDto.toArticle(): Article =
   Article(
      id = id,                            // Domain-Article.id (id ist bei dir nullable? ggf. anpassen)
      author = author,
      description = description ?: "",
      publishedAt = publishedAt ?: "",
      title = title ?: "",
      url = url ?: "",
      urlToImage = urlToImage ?: "",
      sourceName = source?.name
   )

// Domain -> DTO
fun Article.toArticleDto(): ArticleDto =
   ArticleDto(
      id = id,                            // Domain bestimmt die ID
      author = author,
      content = "",
      description = description,
      publishedAt = publishedAt,
      title = title,
      url = url,
      urlToImage = urlToImage,
      source = SourceDto("",sourceName)
   )

// ---------------------------------------------------------------------
// Article: Room DTO ⇄ Domain
// ---------------------------------------------------------------------
// DTO -> Domain
fun ArticleRoomDto.toArticle(): Article =
   Article(
      id = id,
      author = author,
      description = description ?: "",
      publishedAt = publishedAt ?: "",
      title = title ?: "",
      url = url ?: "",
      urlToImage = urlToImage ?: "",
      sourceName = sourceName
   )

// Domain -> DTO
fun Article.toArticleRoomDto(): ArticleRoomDto =
   ArticleRoomDto(
      id = id,                            // Domain bestimmt die ID
      author = author,
      description = description,
      publishedAt = publishedAt,
      title = title,
      url = url,
      urlToImage = urlToImage,
      sourceName = sourceName
   )


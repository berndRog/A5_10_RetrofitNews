package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.local.dtos.ArticleRoomDto
import de.rogallab.mobile.data.mappings.toArticle
import de.rogallab.mobile.data.mappings.toArticleRoomDto
import de.rogallab.mobile.domain.IArticleRepository
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class ArticleRepository(
   private val _articleDao: IArticleDao,
   private val _dispatcher: CoroutineDispatcher
) : IArticleRepository {

   override fun selectArticles(): Flow<Result<List<Article>>> =
      _articleDao.select()
         .map { articleRoomDtos: List<ArticleRoomDto> ->
            logDebug(TAG, "selectArticles() ${articleRoomDtos.size}")
            Result.success(articleRoomDtos.map { it.toArticle() })
         }
         .catch { t ->
            if (t is CancellationException) { throw t }
            emit(Result.failure(t))  // <-- `emit` IS required here
         }

   // Upsert (insert or update) a single article.
   override suspend fun upsert(article: Article): Result<Unit> =
      try {
         logDebug(TAG, "upsert article")
         _articleDao.upsert(article.toArticleRoomDto())
         Result.success(Unit)
      } catch (t: Throwable) {
         if (t is CancellationException) { throw t }
         Result.failure(t)
      }

   // Remove a single article.
   override suspend fun remove(article: Article): Result<Unit> =
      try {
         logDebug(TAG, "delete article")
         _articleDao.remove(article.toArticleRoomDto())
         Result.success(Unit)
      } catch (t: Throwable) {
         if (t is CancellationException) { throw t }
         Result.failure(t)
      }


   companion object {
      private const val TAG = "<-ArticleRepository"
   }
}
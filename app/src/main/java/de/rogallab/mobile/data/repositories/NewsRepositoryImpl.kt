package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.INewsWebservice
import de.rogallab.mobile.data.models.Article
import de.rogallab.mobile.data.models.News
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
   private val _articleDao: IArticleDao,
   private val _newsWebservice: INewsWebservice,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : INewsRepository {

   override suspend fun getHeadlines(country: String, page: Int): Response<News> =
      withContext(_dispatcher+_exceptionHandler) {
         // throw Exception("Test: throw Exception")
         logDebug(tag, "getHeadlines country:$country page:$page")
         return@withContext _newsWebservice.getHeadlines(country, page)
      }

   override suspend fun getEverything(searchText: String, page: Int): Response<News> =
      withContext(_dispatcher+_exceptionHandler) {
         //throw Exception("Test: throw Exception")
         logDebug(tag, "getEverything search:$searchText, page:$page")
         return@withContext _newsWebservice.getEverything(searchText, page)
      }

   override fun selectArticles(): Flow<List<Article>> {
      logDebug(tag, "selectArticles")
      return _articleDao.select()
   }

   override suspend fun upsert(article: Article) : Boolean =
      withContext(_dispatcher + _exceptionHandler) {
         logDebug(tag, "upsert article")
         _articleDao.upsert(article)
         return@withContext true
      }

   override suspend fun delete(article: Article): Boolean =
      withContext(_dispatcher + _exceptionHandler) {
         logDebug(tag, "delete article")
         _articleDao.delete(article)
         return@withContext true
      }

   companion object {
                             //12345678901234567890123
      private const val tag = "ok>NewsRepositoryImpl ."
   }
}
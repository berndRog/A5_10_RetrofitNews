package de.rogallab.mobile.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import de.rogallab.mobile.data.INewsApi
import de.rogallab.mobile.data.remote.dtos.NewsDto
import de.rogallab.mobile.data.mappings.toArticle
import de.rogallab.mobile.data.remote.dtos.ArticleDto
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.features.news.NewsPagingSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class NewsRepository(
   private val _newsApi: INewsApi,
   private val _dispatcher: CoroutineDispatcher
) : INewsRepository {

   //--- Without paging: one-shot "everything" call wrapped as Flow<Result<List<Article>> ----------
   override fun getEverything(
      searchText: String,
      pageSize: Int,
      sortBy: String
   ): Flow<Result<List<Article>>> = flow {

      // Trim the text to avoid triggering the API for whitespace-only input
      val query = searchText.trim()

      // SHORT-CIRCUIT: if the search text is empty, immediately emit an empty News() object.
      if (query.isBlank()) {
         emit(Result.success(listOf()))
         return@flow  // stop execution of the flow builder
      }

      // Normal API call
      try {
         // Call the remote webservice (suspend function)
         val response = _newsApi.getEverything(
            text = query,
            page = 1,
            pageSize = pageSize,
            sortBy = sortBy
         )

         // Check if the response was successful
         if(response.isSuccessful) {
            // Extract the response body
            val newsDto: NewsDto? = response.body()
            // Check parameters
            if (newsDto != null) {
               val articleDtos: List<ArticleDto> = newsDto.articles
               val articles: List<Article> = articleDtos.map{ it.toArticle() }
               // Emit the successful result with the received news data
               emit(Result.success(articles))
            }
            else {
               // emit(Result.failure(Exception("Empty response body")))
            }
         }
         else {
            emit(Result.failure(Exception(
               "HTTP ${response.code()} ${response.message()}")))
         }
      }
      // CancellationException must be re-thrown
      catch (e: CancellationException) { throw e }
      // Any other exception is converted into a failure Result.
      catch (t: Throwable) { emit(Result.failure(t)) }

      // Ensure the entire pipeline runs on the provided dispatcher (usually Dispatchers.IO)
   } .flowOn(_dispatcher)

   //--- With paging: infinite scroll using Paging 3 -----------------------------------------------
   override fun getEverythingPaged(
      searchText: String,
      pageSize: Int,
      sortBy: String
   ): Flow<PagingData<Article>> =
      Pager(
         config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false
         ),
         pagingSourceFactory = {
            // We keep your PagingSource untouched!
            NewsPagingSource(
               api = _newsApi,
               query = searchText,
               pageSize = pageSize,
               sortBy = sortBy
            )
         }
      ).flow.map { pagingData ->
         pagingData.map { dto -> dto.toArticle() }
      }


   companion object {
      private const val TAG = "<-NewsRepository"
   }
}
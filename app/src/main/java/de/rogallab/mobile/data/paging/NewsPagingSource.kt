package de.rogallab.mobile.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.rogallab.mobile.data.INewsApi
import de.rogallab.mobile.data.remote.dtos.ArticleDto
import de.rogallab.mobile.data.remote.dtos.NewsDto
import de.rogallab.mobile.domain.utilities.logDebug
import retrofit2.HttpException
import java.io.IOException

class NewsPagingSource(
   private val api: INewsApi,
   private val query: String,
   private val pageSize: Int,
   private val sortBy: String = "publishedAt"
) : PagingSource<Int, ArticleDto>() {

   companion object { private const val TAG = "<-NewsPagingSource" }

   override fun getRefreshKey(state: PagingState<Int, ArticleDto>): Int? {
      // Determines which key to reload when user scrolls or rotates device
      val anchor = state.anchorPosition ?: return null
      val page = state.closestPageToPosition(anchor) ?: return null
      return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
   }

   override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
      val page = params.key ?: 1

      return try {
         logDebug(TAG, "load (page=$page, query='$query', sortBy=$sortBy)")

         // Call REST API with page, pageSize and sortBy
         val response = api.getEverything(text = query, page = page,
            pageSize = pageSize, sortBy = sortBy)

         // Handle HTTP errors
         if (!response.isSuccessful)
            return LoadResult.Error(HttpException(response))

         // Get the payload
         val body: NewsDto = response.body()
            // if null return empty list
            ?: return LoadResult.Page(
               data = emptyList(),
               prevKey = if (page == 1) null else page - 1,
               nextKey = null
            )

         val articleDtos: List<ArticleDto> = body.articles

         // Pagination logic: stop if no more data
         LoadResult.Page(
            data = articleDtos,
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (articleDtos.size < pageSize) null else page + 1
         )
      }
      catch (e: IOException) { LoadResult.Error(e) }      // Network failure
      catch (e: HttpException) { LoadResult.Error(e) }     // 4xx/5xx
      catch (e: Throwable) { LoadResult.Error(e) }         // Anything else
   }
}
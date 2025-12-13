package de.rogallab.mobile.test.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.rogallab.mobile.data.INewsApi
import de.rogallab.mobile.data.paging.NewsPagingSource
import de.rogallab.mobile.data.remote.dtos.ArticleDto
import de.rogallab.mobile.test.data.BaseDataKoinTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import okhttp3.Headers
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException

@OptIn(ExperimentalCoroutinesApi::class)
class NewsPagingSourceIntegrationTest : BaseDataKoinTest() {

   private val pageSize = 5
   private val query = "android"
   private val sortBy = "publishedAt"

   @Test
   fun `load page1 returns 5 articles and nextKey=2`() = runTest {
      // Arrange
      _mockWebServer.enqueue(
         MockResponse(
            code = 200,
            headers = Headers.headersOf("Content-Type", "application/json"),
            body = _seed.newsPage(1)
         )
      )

      val api = _koin.get<INewsApi>()
      val pagingSource = NewsPagingSource(api, query, pageSize, sortBy)

      // Act
      val result = pagingSource.load(
         PagingSource.LoadParams.Refresh(
            key = null, // page 1
            loadSize = pageSize,
            placeholdersEnabled = false
         )
      )

      // Assert
      val page = result as PagingSource.LoadResult.Page
      assertEquals(5, page.data.size)
      assertNull(page.prevKey)
      assertEquals(2, page.nextKey)

      val req = _mockWebServer.takeRequest()
      val q = req.url.encodedQuery.orEmpty()
      assertTrue("expected page=1 in query but was: '$q'", q.contains("page=1"))
      assertTrue("expected pageSize=$pageSize in query but was: '$q'", q.contains("pageSize=$pageSize"))
      assertTrue("expected sortBy=$sortBy in query but was: '$q'", q.contains("sortBy=$sortBy"))
   }

   @Test
   fun `load page2 returns 5 articles prevKey=1 nextKey=3`() = runTest {
      // Arrange
      _mockWebServer.enqueue(
         MockResponse(
            code = 200,
            headers = Headers.headersOf("Content-Type", "application/json"),
            body = _seed.newsPage(2)
         )
      )

      val api = _koin.get<INewsApi>()
      val pagingSource = NewsPagingSource(api, query, pageSize, sortBy)

      // Act
      val result = pagingSource.load(
         PagingSource.LoadParams.Refresh(
            key = 2,
            loadSize = pageSize,
            placeholdersEnabled = false
         )
      )

      // Assert
      val page = result as PagingSource.LoadResult.Page
      assertEquals(5, page.data.size)
      assertEquals(1, page.prevKey)
      assertEquals(3, page.nextKey)

      val req = _mockWebServer.takeRequest()
      val q = req.url.encodedQuery.orEmpty()
      assertTrue("expected page=2 in query but was: '$q'", q.contains("page=2"))
   }

   @Test
   fun `load page3 returns 1 article prevKey=2 and nextKey=null because last page`() = runTest {
      // Arrange: last page has only 1 item (< pageSize)
      _mockWebServer.enqueue(
         MockResponse(
            code = 200,
            headers = Headers.headersOf("Content-Type", "application/json"),
            body = _seed.newsPage(3)
         )
      )

      val api = _koin.get<INewsApi>()
      val pagingSource = NewsPagingSource(api, query, pageSize, sortBy)

      // Act
      val result = pagingSource.load(
         PagingSource.LoadParams.Refresh(
            key = 3,
            loadSize = pageSize,
            placeholdersEnabled = false
         )
      )

      // Assert
      val page = result as PagingSource.LoadResult.Page
      assertEquals(1, page.data.size)
      assertEquals(2, page.prevKey)
      assertNull(page.nextKey) // <-- angepasst an neue Logik: size < pageSize => null
   }

   @Test
   fun `load http 400 returns LoadResult_Error HttpException`() = runTest {
      // Arrange
      _mockWebServer.enqueue(
         MockResponse(
            code = 400,
            headers = Headers.headersOf("Content-Type", "application/json"),
            body = """{"status":"error","message":"Bad Request"}"""
         )
      )

      val api = _koin.get<INewsApi>()
      val pagingSource = NewsPagingSource(api, query, pageSize, sortBy)

      // Act
      val result = pagingSource.load(
         PagingSource.LoadParams.Refresh(
            key = 1,
            loadSize = pageSize,
            placeholdersEnabled = false
         )
      )

      // Assert
      assertTrue(result is PagingSource.LoadResult.Error)
      val err = result as PagingSource.LoadResult.Error
      assertTrue(err.throwable is HttpException)
   }

   @Test
   fun `getRefreshKey returns prevKey plus 1`() {
      val api = _koin.get<INewsApi>()
      val pagingSource = NewsPagingSource(api, query, pageSize, sortBy)

      // wichtig: nicht emptyList(), sonst findet closestPageToPosition oft keine Page
      val pages = listOf(
         PagingSource.LoadResult.Page(
            data = listOf(dummyArticleDto()),
            prevKey = 1,
            nextKey = 3
         )
      )

      val state = PagingState(
         pages = pages,
         anchorPosition = 0,
         config = PagingConfig(pageSize = pageSize),
         leadingPlaceholderCount = 0
      )

      val key = pagingSource.getRefreshKey(state)
      assertEquals(2, key)
   }

   @Test
   fun `getRefreshKey returns nextKey minus 1 when prevKey is null`() {
      val api = _koin.get<INewsApi>()
      val pagingSource = NewsPagingSource(api, query, pageSize, sortBy)

      val pages = listOf(
         PagingSource.LoadResult.Page(
            data = listOf(dummyArticleDto()),
            prevKey = null,
            nextKey = 5
         )
      )

      val state = PagingState(
         pages = pages,
         anchorPosition = 0,
         config = PagingConfig(pageSize = pageSize),
         leadingPlaceholderCount = 0
      )

      val key = pagingSource.getRefreshKey(state)
      assertEquals(4, key)
   }

   private fun dummyArticleDto(): ArticleDto =
      ArticleDto(
         title = "dummy",
         description = "dummy",
         url = "https://example.com/dummy",
         urlToImage = "https://example.com/dummy.jpg",
         publishedAt = "2025-12-10T08:00:00Z"
      )

}
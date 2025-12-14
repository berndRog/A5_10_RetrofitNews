package de.rogallab.mobile.test.data.repositories

import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.test.data.BaseDataKoinTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import okhttp3.Headers.Companion.headersOf
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class INewsRepositoryTest : BaseDataKoinTest() {

   private lateinit var repo: INewsRepository

   @Before
   fun setUpTest() {
      repo = _koin.get()
   }

   // ---------------------------------------------------------------------------------------------
   // getEverything (non-paging)
   // ---------------------------------------------------------------------------------------------

   @Test
   fun getEverything_blank_searchText_emits_empty_list_and_does_not_call_API() =
      runTest(mainDispatcherRule.dispatcher()) {
         val result = repo.getEverything(searchText = "   ").first()

         assertTrue(result.isSuccess)
         assertEquals(0, result.getOrThrow().size)

         val req = _mockWebServer.takeRequest(250, TimeUnit.MILLISECONDS)
         assertNull(req)
      }

   @Test
   fun getEverything_success_emits_mapped_list() =
      runTest(mainDispatcherRule.dispatcher()) {
         _mockWebServer.enqueue(
            MockResponse(
               code = 200,
               headers = headersOf("Content-Type","application/json"),
               body = _seed.jsonListOfArticles
            )
         )

         val result = repo.getEverything(
            searchText = "Hannover",
            pageSize = 20,
            sortBy = "publishedAt"
         ).first()

         assertTrue(result.isSuccess)
         val articles = result.getOrThrow()
         assertEquals(_seed.articles.size, articles.size)
         assertEquals("Test title 1", articles.first().title)
      }

   @Test
   fun getEverything_http_500_emits_failure() =
      runTest(mainDispatcherRule.dispatcher()) {
         _mockWebServer.enqueue(
            MockResponse(
               code = 500,
               headers = headersOf("Content-Type","application/json"),
               body = """{"status":"error","message":"server error"}"""
            )
         )

         val result = repo.getEverything(searchText = "ErrorCase").first()

         assertTrue(result.isFailure)
         assertNotNull(result.exceptionOrNull())
      }
}

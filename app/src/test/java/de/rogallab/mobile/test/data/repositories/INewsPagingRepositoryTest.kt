package de.rogallab.mobile.test.data.repositories

import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.test.data.BaseDataKoinTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import org.junit.Before
import org.junit.Test
import okhttp3.Headers.Companion.headersOf
import kotlin.test.assertEquals
import androidx.paging.testing.*
import kotlin.test.assertFails
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class INewsPagingRepositoryTest : BaseDataKoinTest() {

   private lateinit var repo: INewsRepository

   @Before
   fun setUpTest() {
      repo = _koin.get()
   }

   @Test
   fun getEverythingPaged_loads_all_11_articles_across_pages() =
      runTest {
         // Enqueue multiple responses for different pages
         _mockWebServer.enqueue(
            MockResponse(code = 200, body = _seed.newsPage(1))
         )
         _mockWebServer.enqueue(
            MockResponse(code = 200, body = _seed.newsPage(2))
         )
         _mockWebServer.enqueue(
            MockResponse(code = 200, body = _seed.newsPage(3))
         )
         _mockWebServer.enqueue(
            MockResponse(code = 200, body = _seed.newsPage(4))
         )

         val pagingData = repo.getEverythingPaged(
            searchText = "android",
            pageSize = 5,
            sortBy = "publishedAt"
         )
         val items = pagingData.asSnapshot {
            // Scroll bis zum Ende, um alle Seiten zu laden
            scrollTo(index = 10)
         }

         assertEquals(11, items.size)
         assertEquals("Test title 1", items.first().title)
         assertEquals("Test title 11", items.last().title)
      }

   @Test
   fun getEverythingPaged_when_page2_returns_http500_snapshot_throws() =
      runTest(mainDispatcherRule.dispatcher()) {

         // page1 OK
         _mockWebServer.enqueue(
            MockResponse(200, headersOf("Content-Type","application/json"), _seed.newsPage(1))
         )
         // page2 FAIL -> Paging should surface error
         _mockWebServer.enqueue(
            MockResponse(
               code = 500,
               headers = headersOf("Content-Type","application/json"),
               body = """{"status":"error","message":"server error"}"""
            )
         )

         val flow = repo.getEverythingPaged(
            searchText = "android",
            pageSize = 5,
            sortBy = "publishedAt"
         )

         // asSnapshot should throw when Paging encounters an error while scrolling/loading
         assertFails {
            flow.asSnapshot {
               // force loading the next page
               scrollTo(index = 6)
            }
         }
      }

   @Test
   fun getEverythingPaged_when_first_page_empty_returns_empty_list() = runTest(mainDispatcherRule.dispatcher()) {
      // SeedTestdata.newsPage(4) returns empty list -> we use it as "empty first page"
      _mockWebServer.enqueue(MockResponse(code = 200, body = _seed.newsPage(4)))

      val flow = repo.getEverythingPaged(
         searchText = "android",
         pageSize = 5,
         sortBy = "publishedAt"
      )

      val items = flow.asSnapshot { /* no scroll needed */ }

      assertEquals(0, items.size)
   }

}

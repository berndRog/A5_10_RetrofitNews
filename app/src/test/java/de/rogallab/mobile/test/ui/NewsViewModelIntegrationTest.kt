package de.rogallab.mobile.ui.features.news

import app.cash.turbine.test
import de.rogallab.mobile.test.data.BaseDataKoinTest
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.NewsList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import org.junit.Test
import org.koin.core.parameter.parametersOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelIntegrationTest : BaseDataKoinTest() {

   @Test
   fun `End-to-End reload loads articles into UiState`() = runTest {
      // Arrange: MockWebServer returns Seed JSON
      _mockWebServer.enqueue(
         MockResponse(
            code = 200,
            headers = okhttp3.Headers.headersOf("Content-Type", "application/json"),
            body = _seed.jsonListOfArticles
         )
      )

      val navHandler: INavHandler = _koin.get { parametersOf(NewsList) }
      val viewModel: NewsViewModel = _koin.get { parametersOf(navHandler) }

      viewModel.newsUiStateFlow.test {
         // 0) initial emission
         val init = awaitItem()
         assertEquals("", init.searchText)
         assertFalse(init.loading)
         assertTrue(init.articles.size == 0)

         // 1) Set a non-blank query (otherwise many repos short-circuit -> no HTTP call)
         viewModel.onProcessIntent(NewsIntent.SearchTextChange("Android"))
         val changed = awaitItem()
         assertEquals("Android", changed.searchText)
         assertFalse(changed.loading)

         // 2) Trigger reload
         viewModel.reload()

         // 3) expect loading=true (from onStart in the pipeline)
         val loading = awaitItem()
         assertTrue(loading.loading)

         // 4) expect final state with articles
         val result = awaitItem()
         assertFalse(result.loading)
         assertEquals(3, result.articles?.size)

         assertEquals("Test title 1", result.articles?.get(0)?.title)
         assertEquals("Test title 2", result.articles?.get(1)?.title)
         assertEquals("Test title 3", result.articles?.get(2)?.title)

         // Now the server should have been called exactly once
         assertEquals(1, _mockWebServer.requestCount)

         cancelAndIgnoreRemainingEvents()
      }
   }

   // -----------------------------------------------------------------------
   // SearchTextChange -> only UI state change, no request to MockWebServer
   // -----------------------------------------------------------------------
   @Test
   fun `SearchTextChange updates UiState but does not call API`() = runTest {
      val navHandler: INavHandler = _koin.get { parametersOf(NewsList) }
      val viewModel: NewsViewModel = _koin.get { parametersOf(navHandler) }

      viewModel.newsUiStateFlow.test {
         val init = awaitItem()
         assertEquals("", init.searchText)
         assertEquals(0, _mockWebServer.requestCount)

         // WHEN
         viewModel.onProcessIntent(NewsIntent.SearchTextChange("Android"))

         // THEN: UiState changed, but still no HTTP call (no reload)
         val changed = awaitItem()
         assertEquals("Android", changed.searchText)
         assertEquals(0, _mockWebServer.requestCount)

         cancelAndIgnoreRemainingEvents()
      }
   }

   // -----------------------------------------------------------------------
   // Error case (HTTP 500)
   // -----------------------------------------------------------------------
   @Test
   fun `Server error results in loading false and null articles`() = runTest {
      _mockWebServer.enqueue(
         MockResponse(
            code = 500,
            body = """{"status":"error"}"""
         )
      )

      val navHandler: INavHandler = _koin.get { parametersOf(NewsList) }
      val viewModel: NewsViewModel = _koin.get { parametersOf(navHandler) }

      viewModel.newsUiStateFlow.test {
         // 0) initial
         val init = awaitItem()
         assertFalse(init.loading)
         assertTrue(init.articles.size == 0)

         // 1) Non-blank query so repository actually calls the API
         viewModel.onProcessIntent(NewsIntent.SearchTextChange("Android"))
         val changed = awaitItem()
         assertEquals("Android", changed.searchText)

         // 2) reload
         viewModel.reload()

         // 3) loading=true
         val loading = awaitItem()
         assertTrue(loading.loading)

         // 4) final state: loading=false, articles still null
         val final = awaitItem()
         assertFalse(final.loading)
         assertTrue(final.articles.size == 0)

         assertEquals(1, _mockWebServer.requestCount)

         cancelAndIgnoreRemainingEvents()
      }
   }
}

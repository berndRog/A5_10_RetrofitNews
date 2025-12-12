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
      // Arrange
      // MockWebServer: return Seed JSON
      _mockWebServer.enqueue(
         MockResponse(
            code = 200,
            headers = okhttp3.Headers.headersOf("Content-Type", "application/json"),
            body = _seed.jsonListOfArticles
         )
      )
      // Create NavHandler / Viewmodel from factory - different for each test case
      val navHandler: INavHandler = _koin.get { parametersOf(NewsList) }
      val viewModel: NewsBaseViewModel = _koin.get { parametersOf(navHandler) }

      // ACT + ASSERT
      viewModel.newsUiStateFlow.test {

         // Trigger loading
         viewModel.reload()

         // 1) expect loading=true
         val loading = awaitItem()
         //assertTrue(loading.loading)

         // 2) expect result
         val result = awaitItem()
         //assertFalse(result.loading)
         //assertEquals(3, result.articles?.size)

         assertEquals("Test title 1", result.articles?.get(0)?.title)
         assertEquals("Test title 2", result.articles?.get(1)?.title)
         assertEquals("Test title 3", result.articles?.get(2)?.title)

         cancelAndIgnoreRemainingEvents()
      }
   }

   // -----------------------------------------------------------------------
   // SearchTextChange -> nur UI-Änderung, kein Request an MockWebServer
   // -----------------------------------------------------------------------
   @Test
   fun `SearchTextChange updates UiState but does not call API`() = runTest {
      // Create NavHandler / Viewmodel from factory - different for each test case
      val navHandler: INavHandler = _koin.get { parametersOf(null) }
      val viewModel: NewsBaseViewModel = _koin.get { parametersOf(navHandler) }

      viewModel.newsUiStateFlow.test {
         val init = awaitItem()
         assertEquals("", init.searchText)

         // WHEN
         viewModel.onProcessIntent(
            NewsIntent.SearchTextChange("Android")
         )

         val changed = awaitItem()
         assertEquals("Android", changed.searchText)
         assertEquals(0, _mockWebServer.requestCount)

         cancelAndIgnoreRemainingEvents()
      }
   }

   // -----------------------------------------------------------------------
   // Fehlerfall (HTTP 500)
   // -----------------------------------------------------------------------
   @Test
   fun `Server error results in loading false and empty articles`() = runTest {

      _mockWebServer.enqueue(
         MockResponse(
            code = 500,
            body = """{"status":"error"}"""
         )
      )

      // Create NavHandler / Viewmodel from factory - different for each test case
      val navHandler: INavHandler = _koin.get { parametersOf(null) }
      val viewModel: NewsBaseViewModel = _koin.get { parametersOf(navHandler) }

      viewModel.newsUiStateFlow.test {

         viewModel.reload()

         val loading = awaitItem()
         assertTrue(loading.loading)

         val final = awaitItem()
         assertFalse(final.loading)
         assertTrue(final.articles == null)

         cancelAndIgnoreRemainingEvents()
      }
   }
}

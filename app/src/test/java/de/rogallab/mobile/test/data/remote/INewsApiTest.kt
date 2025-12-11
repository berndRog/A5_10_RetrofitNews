package de.rogallab.mobile.test.data.remote

import de.rogallab.mobile.data.INewsApi
import de.rogallab.mobile.data.remote.dtos.NewsDto
import de.rogallab.mobile.test.TestApplication
import de.rogallab.mobile.test.data.BaseDataKoinTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import okhttp3.Headers.Companion.headersOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class)
class INewsApiTest: BaseDataKoinTest() {

   private lateinit var _newsApi: INewsApi

   @Before
   fun setUpTest() {
      _newsApi = _koin.get<INewsApi>()
   }

   @Test
   fun `getEverything sends correct request and parses response body`() = runTest {
      // --- Arrange ---------------------------------------------------------
      val bodyJson = _seed.jsonListOfArticles
      _mockWebServer.enqueue(
         MockResponse(
            code = 200,
            headers = headersOf("Content-Type", "application/json"),
            body = bodyJson
         )
      )

      val searchText = "Hannover"
      val page = 1
      val pageSize = 5
      val sortBy = "publishedAt"

      // --- Act -------------------------------------------------------------
      val response: Response<NewsDto> = _newsApi.getEverything(
         text = searchText,
         page = page,
         pageSize = pageSize,
         sortBy = sortBy
      )

      // --- Assert: HTTP/DTO side ------------------------------------------
      assertTrue("Response should be successful", response.isSuccessful)
      assertEquals(200, response.code())

      val body = response.body()
      assertNotNull("Body should not be null", body)
      body!!

      assertEquals("ok", body.status)
      assertEquals(3, body.articles.size)
      assertEquals("Test title 1", body.articles[0].title)
      assertEquals("Test description 1", body.articles[0].description)

      // --- Assert: outgoing request (path + query) ------------------------
      val recorded = _mockWebServer.takeRequest()

      // Path (without query parameters)
      assertEquals("/v2/everything", recorded.url.encodedPath)

      // Query string only
      assertEquals(
         "q=$searchText&page=$page&pagesize=$pageSize&sortBy=$sortBy",
         recorded.url.encodedQuery
      )

      // HTTP method
      assertEquals("GET", recorded.method)
   }

   @Test
   fun `getEverything returns error response for http 401`() = runTest {
      // --- Arrange ---------------------------------------------------------
      val errorBody = """
         {
           "status": "error",
           "code": "apiKeyInvalid",
           "message": "Your API key is invalid."
         }
      """.trimIndent()

      _mockWebServer.enqueue(
         MockResponse(
            code = 401,
            headers = headersOf("Content-Type", "application/json"),
            body = errorBody
         )
      )

      // --- Act -------------------------------------------------------------
      val response = _newsApi.getEverything(text = "Berlin")

      // --- Assert ----------------------------------------------------------
      assertFalse("Response should not be successful", response.isSuccessful)
      assertEquals(401, response.code())

      val recorded = _mockWebServer.takeRequest()
      assertEquals("/v2/everything", recorded.url.encodedPath)
      assertEquals(
         "q=Berlin&page=1&pagesize=20&sortBy=publishedAt",
         recorded.url.encodedQuery
      )
   }
}

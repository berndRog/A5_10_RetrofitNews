package de.rogallab.mobile.test.data.remote

import de.rogallab.mobile.test.TestApplicationWithoutKoin
import de.rogallab.mobile.data.INewsApi
import de.rogallab.mobile.data.remote.dtos.NewsDto
import de.rogallab.mobile.data.remote.network.ApiKeyInterceptor
import de.rogallab.mobile.data.remote.network.ApiKeyMode
import de.rogallab.mobile.data.remote.network.BearerTokenInterceptor
import de.rogallab.mobile.data.remote.network.ConnectivityInterceptor
import de.rogallab.mobile.data.remote.network.INetworkConnection
import de.rogallab.mobile.data.remote.network.createOkHttpClient
import de.rogallab.mobile.data.remote.network.createRetrofit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(
   sdk = [34],
   application = TestApplicationWithoutKoin::class // prevents Koin from starting automatically
)
class NewsApiRetrofitStackTest {

   private lateinit var _mockWebServer: MockWebServer
   private lateinit var _retrofit: Retrofit
   private lateinit var _client: OkHttpClient
   private lateinit var _api: INewsApi

   // Test constants
   private val _testApiKey = "TEST_API_KEY"
   private val _testBearerToken = "TEST_BEARER_TOKEN"

   @Before
   fun setup() {
      _mockWebServer = MockWebServer()
      _mockWebServer.start()
   }

   @After
   fun teardown() {
      _mockWebServer.shutdown()
   }

   // ---------------------------------------------------------------------
   // Helper builders
   // ---------------------------------------------------------------------
   private fun buildClient(
      mode: ApiKeyMode,
      apiKeyProvider: () -> String? = { _testApiKey },
      bearerTokenProvider: () -> String? = { _testBearerToken }
   ): OkHttpClient {
      class NetworkConnectionMock: INetworkConnection {
         override fun isOnline(): Boolean = true
      }
      val connectivityInterceptor = ConnectivityInterceptor(
         NetworkConnectionMock()
      )

      val apiKeyInterceptor = ApiKeyInterceptor(
         _keyProvider = apiKeyProvider,
         _mode = mode,
         _queryName = "apiKey"
      )

      val bearerInterceptor = BearerTokenInterceptor(
         _tokenProvider = bearerTokenProvider
      )

      val loggingInterceptor = HttpLoggingInterceptor().apply {
         level = HttpLoggingInterceptor.Level.BODY
      }

      return createOkHttpClient(
         connectivityInterceptor = connectivityInterceptor,
         apiKeyInterceptor = apiKeyInterceptor,
         bearerTokenInterceptor = bearerInterceptor,
         loggingInterceptor = loggingInterceptor
      )
   }

   private fun buildRetrofit(
      okHttpClient: OkHttpClient
   ): Retrofit {
      return createRetrofit(
         baseUrl = _mockWebServer.url("/").toString(),
         okHttpClient = okHttpClient
      )
   }

   // Simple reusable MockWebServer JSON response
   private fun enqueueNewsResponse() {
      _mockWebServer.enqueue(
         MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(
               """
               {
                 "status": "ok",
                 "totalResults": 1,
                 "articles": [
                   {
                     "title": "Test title",
                     "description": "Test description",
                     "url": "https://example.com/article",
                     "urlToImage": "https://example.com/image.jpg",
                     "publishedAt": "2025-12-10T12:34:56Z"
                   }
                 ]
               }
               """.trimIndent()
            )
      )
   }
   // Simple reusable MockWebServer JSON response
   private fun enqueueNews3Response() {
      _mockWebServer.enqueue(
         MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(
               """
               {
                 "status": "ok",
                 "totalResults": 3,
                 "articles": [
                   {
                     "title": "Test title 1",
                     "description": "Test description 1",
                     "url": "https://example.com/article1",
                     "urlToImage": "https://example.com/image1.jpg",
                     "publishedAt": "2025-12-10T08:00:00Z"
                   },
                   {
                     "title": "Test title 2",
                     "description": "Test description 3",
                     "url": "https://example.com/article2",
                     "urlToImage": "https://example.com/image3.jpg",
                     "publishedAt": "2025-12-10T09:00:00Z"
                   },
                   {
                     "title": "Test title 3",
                     "description": "Test description 3",
                     "url": "https://example.com/article3",
                     "urlToImage": "https://example.com/image3.jpg",
                     "publishedAt": "2025-12-10T10:00:00Z"
                   }
                 ]
               }
               """.trimIndent()
            )
      )
   }

   // ---------------------------------------------------------------------
   // 1) Retrofit with HEADER API-Key injection
   // ---------------------------------------------------------------------
   @Test
   fun `Header mode - API key added as header, parsing successful`() = runBlocking {
      _client = buildClient(mode = ApiKeyMode.HEADER)
      _retrofit = buildRetrofit(_client)
      _api = _retrofit.create(INewsApi::class.java)

      enqueueNewsResponse()

      val response = _api.getEverything("Hannover", 1, 10, "publishedAt")

      // --- Validate JSON → DTO parsing ---
      assertTrue(response.isSuccessful)
      val body = response.body()
      assertNotNull(body)
      assertEquals("ok", body.status)
      assertEquals(1, body.totalResults)
      assertEquals("Test title", body.articles.first().title)

      // --- Validate outgoing request ---
      val request: RecordedRequest = _mockWebServer.takeRequest()
      val url = request.requestUrl!!
      assertEquals("/v2/everything", url.encodedPath)

      // query parameters from interface
      assertEquals("Hannover", url.queryParameter("q"))
      assertEquals("1", url.queryParameter("page"))
      assertEquals("10", url.queryParameter("pagesize"))
      assertEquals("publishedAt", url.queryParameter("sortBy"))

      // API-key must be in header, NOT query
      assertEquals(_testApiKey, request.getHeader("X-API-Key"))
      assertNull(url.queryParameter("apiKey"))

      // Bearer token must be present
      assertEquals("Bearer $_testBearerToken", request.getHeader("Authorization"))
   }

   // 2) Retrofit with QUERY API-Key injection
   @Test
   fun `Query mode - API key added as query parameter, header absent`() = runBlocking {
      _client = buildClient(mode = ApiKeyMode.QUERY)
      _retrofit = buildRetrofit(_client)
      _api = _retrofit.create(INewsApi::class.java)

      enqueueNewsResponse()

      val response = _api.getEverything("Berlin", 2, 5, "publishedAt")

      assertTrue(response.isSuccessful)
      val body = response.body()
      assertNotNull(body)
      assertEquals("ok", body.status)

      val request: RecordedRequest = _mockWebServer.takeRequest()
      val url = request.requestUrl!!

      // API key in query
      assertEquals(_testApiKey, url.queryParameter("apiKey"))
      // header must NOT be set
      assertNull(request.getHeader("X-API-Key"))

      assertEquals("Bearer $_testBearerToken", request.getHeader("Authorization"))
   }

   // 3) No API-Key provided → must not inject
   @Test
   fun `No API key - neither header nor query injected, bearer remains`() = runBlocking {
      _client = buildClient(mode = ApiKeyMode.HEADER, apiKeyProvider = { null })
      _retrofit = buildRetrofit(_client)
      _api = _retrofit.create(INewsApi::class.java)

      enqueueNewsResponse()

      val response = _api.getEverything("Hamburg", 1, 20, "publishedAt")
      assertTrue(response.isSuccessful)

      val request = _mockWebServer.takeRequest()
      val url = request.requestUrl!!

      // No API key anywhere
      assertNull(request.getHeader("X-API-Key"))
      assertNull(url.queryParameter("apiKey"))

      // Bearer token still injected
      assertEquals("Bearer $_testBearerToken", request.getHeader("Authorization"))
   }

   // 4) Invalid API key → 401 Unauthorized
   @Test
   fun `Header mode - invalid API key results in 401 error`() = runBlocking {
      // Arrange: client with INVALID api key
      _client = buildClient(
         mode = ApiKeyMode.HEADER,
         apiKeyProvider = { "INVALID_KEY" }
      )
      _retrofit = buildRetrofit(_client)
      _api = _retrofit.create(INewsApi::class.java)

      // Simulate a 401 response from the server
      _mockWebServer.enqueue(
         MockResponse()
            .setResponseCode(401)
            .setHeader("Content-Type", "application/json")
            .setBody(
               """
            {
              "status": "error",
              "code": "apiKeyInvalid",
              "message": "Your API key is invalid."
            }
            """.trimIndent()
            )
      )

      // Act
      val response = _api.getEverything("Hannover", 1, 10, "publishedAt")

      // Assert - HTTP error response
      assertFalse(response.isSuccessful)
      assertEquals(401, response.code())

      // Optional: inspect error body text (for debugging or logging)
      val errorBodyText = response.errorBody()?.string()
      assertNotNull(errorBodyText)
      // You could assert partial content if you like:
      // assertTrue(errorBodyText.contains("apiKeyInvalid"))

      // Assert - request still injected the header
      val request = _mockWebServer.takeRequest()
      val url = request.requestUrl!!
      assertEquals("/v2/everything", url.encodedPath)

      // Invalid key is still injected as header
      assertEquals("INVALID_KEY", request.getHeader("X-API-Key"))
      // No query param in header mode
      assertNull(url.queryParameter("apiKey"))

      // Bearer token should still be present
      assertEquals("Bearer $_testBearerToken", request.getHeader("Authorization"))
   }

   // 5) Server returns 500 → internal error
   @Test
   fun `Server error 500 - response is unsuccessful and body is null`() = runBlocking {
      // Arrange
      _client = buildClient(mode = ApiKeyMode.HEADER)
      _retrofit = buildRetrofit(_client)
      _api = _retrofit.create(INewsApi::class.java)

      _mockWebServer.enqueue(
         MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody(
               """
            {
              "status": "error",
              "code": "serverError",
              "message": "Internal server error."
            }
            """.trimIndent()
            )
      )

      // Act
      val response = _api.getEverything("ErrorCase", 1, 20, "publishedAt")

      // Assert
      assertFalse(response.isSuccessful)
      assertEquals(500, response.code())

      // For 5xx errors, you normally should not rely on body structure,
      // but we can still assert that the body is not parsed into NewsDto:
      val body: NewsDto? = response.body()
      assertNull(body)

      val request = _mockWebServer.takeRequest()
      val url = request.requestUrl!!
      assertEquals("/v2/everything", url.encodedPath)

      // API key & bearer token still injected correctly
      assertEquals(_testApiKey, request.getHeader("X-API-Key"))
      assertEquals("Bearer $_testBearerToken", request.getHeader("Authorization"))
   }

   // 6) Network failure → IOException from Retrofit call
   @Test
   fun `Network failure - Retrofit call throws IOException`() {
      // Arrange
      _client = buildClient(mode = ApiKeyMode.HEADER)
      _retrofit = buildRetrofit(_client)
      _api = _retrofit.create(INewsApi::class.java)

      // Simulate a broken connection: disconnect immediately
      _mockWebServer.enqueue(
         MockResponse()
            .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
      )

      // Act + Assert: Retrofit should surface this as an IOException
      assertFailsWith<IOException> {
         runBlocking {
            _api.getEverything("NetworkFail", 1, 10, "publishedAt")
         }
      }

      // No request data to assert here, because the connection never fully completes.
   }


}

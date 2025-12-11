package de.rogallab.mobile.test.data.repositories

import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.test.data.BaseDataKoinTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import okhttp3.Headers.Companion.headersOf
import org.junit.*
import org.junit.Assert.assertEquals

class INewsRepositoryTest : BaseDataKoinTest() {

   private lateinit var _newsRepository: INewsRepository

   @Before
   fun setUpTest() {
      _newsRepository = _koin.get<INewsRepository>()
   }


   @Test
   fun `getEverything returns mapped list of articles from API`() = runTest {
      // --- Arrange ---------------------------------------------------------

      // Feed MockWebServer with your JSON seed
      _mockWebServer.enqueue(
         MockResponse(
            code = 200,
            headers = headersOf("Content-Type", "application/json"),
            body = _seed.jsonListOfArticles
         )
      )

      val searchText = "Hannover"
      val page = 1

      // --- Act -------------------------------------------------------------

      // Collect the first emission from the repository flow
      val result: Result<List<Article>> =
         _newsRepository.getEverything(searchText = searchText).first()

      // --- Assert: Success -------------------------------------------------

      Assert.assertTrue("Expected success Result", result.isSuccess)

      val articles = result.getOrThrow()
      Assert.assertEquals(
         "Number of mapped articles must match seed data",
         _seed.articles.size,
         articles.size
      )

      // Field-by-field comparison using your domain mapping
      _seed.articles.zip(articles).forEachIndexed { index, (expected, actual) ->
         Assert.assertEquals("title mismatch at index $index", expected.title, actual.title)
         Assert.assertEquals("description mismatch at index $index", expected.description, actual.description)
         Assert.assertEquals("url mismatch at index $index", expected.url, actual.url)
         Assert.assertEquals("urlToImage mismatch at index $index", expected.urlToImage, actual.urlToImage)
         Assert.assertEquals("publishedAt mismatch at index $index", expected.publishedAt, actual.publishedAt)
         Assert.assertEquals("sourceName mismatch at index $index", expected.sourceName, actual.sourceName)
      }

      // --- Assert: Did Retrofit send the correct request? ------------------

      val recorded = _mockWebServer.takeRequest()
      // 1) Path
      assertEquals("/v2/everything", recorded.url.encodedPath)
      // 2) Query
      assertEquals(
         "q=Hannover&page=1&pagesize=20&sortBy=publishedAt",
         recorded.url.encodedQuery
      )
      assertEquals("GET", recorded.method)
   }
}

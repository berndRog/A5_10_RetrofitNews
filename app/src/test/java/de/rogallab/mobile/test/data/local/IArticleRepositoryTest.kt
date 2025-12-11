package de.rogallab.mobile.test.data.local

import de.rogallab.mobile.domain.IArticleRepository
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.test.TestApplication
import de.rogallab.mobile.test.data.BaseDataKoinTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class)
class IArticleRepositoryTest : BaseDataKoinTest() {

   private lateinit var _repository: IArticleRepository

   @Before
   fun setUpTest() {
      _repository = _koin.get<IArticleRepository>()
   }

   // -------------------------------------------------------------------------
   // Tests
   // -------------------------------------------------------------------------

   @Test
   fun `selectArticles returns empty list initially`() = runTest {
      // Act
      val result = _repository.selectArticles().first()

      // Assert
      Assert.assertTrue(result.isSuccess)
      val articles = result.getOrThrow()
      Assert.assertTrue("Expected empty list on fresh in-memory DB", articles.isEmpty())
   }

   @Test
   fun `upsert inserts articles and selectArticles returns them`() = runTest {
      // Arrange
      val articlesToInsert: List<Article> = _seed.articles

      // Act
      for (article in articlesToInsert) {
         val upsertResult = _repository.upsert(article)
         Assert.assertTrue("upsert() should succeed", upsertResult.isSuccess)
      }

      val selectResult = _repository.selectArticles().first()

      // Assert
      Assert.assertTrue(selectResult.isSuccess)
      val storedArticles = selectResult.getOrThrow()

      Assert.assertEquals(
         "Number of stored articles must match seed data",
         articlesToInsert.size,
         storedArticles.size
      )

      // Compare field by field (optional but nice)
      articlesToInsert.zip(storedArticles).forEachIndexed { index, (expected, actual) ->
         Assert.assertEquals("id mismatch at index $index", expected.id, actual.id)
         Assert.assertEquals("title mismatch at index $index", expected.title, actual.title)
         Assert.assertEquals("description mismatch at index $index", expected.description, actual.description)
         Assert.assertEquals("url mismatch at index $index", expected.url, actual.url)
         Assert.assertEquals("urlToImage mismatch at index $index", expected.urlToImage, actual.urlToImage)
         Assert.assertEquals("publishedAt mismatch at index $index", expected.publishedAt, actual.publishedAt)
         Assert.assertEquals("sourceName mismatch at index $index", expected.sourceName, actual.sourceName)
      }
   }

   @Test
   fun `remove deletes article from database`() = runTest {
      // Arrange: insert exactly one article
      val article = _seed.articles.first()

      Assert.assertTrue(_repository.upsert(article).isSuccess)

      // Sanity check: article is there
      val beforeResult = _repository.selectArticles().first()
      Assert.assertTrue(beforeResult.isSuccess)
      val beforeList = beforeResult.getOrThrow()
      Assert.assertEquals(1, beforeList.size)

      // Act: remove the article
      val removeResult = _repository.remove(article)
      Assert.assertTrue("remove() should succeed", removeResult.isSuccess)

      // Assert: DB is empty again
      val afterResult = _repository.selectArticles().first()
      Assert.assertTrue(afterResult.isSuccess)
      val afterList = afterResult.getOrThrow()
      Assert.assertTrue("Expected empty list after remove()", afterList.isEmpty())
   }
}

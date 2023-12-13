package de.rogallab.mobile.data

import androidx.room.*
import de.rogallab.mobile.data.models.Article
import kotlinx.coroutines.flow.Flow

@Dao
public interface IArticleDao {

   @Query("SELECT * FROM article")
   public fun select(): Flow<List<Article>>

   @Insert(onConflict = OnConflictStrategy.REPLACE)
   public suspend fun upsert(article: Article): Long

   @Delete
   public suspend fun delete(article: Article)

}
package de.rogallab.mobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.rogallab.mobile.data.local.dtos.ArticleRoomDto
import de.rogallab.mobile.data.remote.dtos.ArticleDto
import kotlinx.coroutines.flow.Flow

@Dao
interface IArticleDao {

   @Query("SELECT * FROM article")
   fun select(): Flow<List<ArticleRoomDto>>

   @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
   suspend fun upsert(articleRoomDto: ArticleRoomDto)

   @Delete
   suspend fun remove(articleRoomDto: ArticleRoomDto)

}
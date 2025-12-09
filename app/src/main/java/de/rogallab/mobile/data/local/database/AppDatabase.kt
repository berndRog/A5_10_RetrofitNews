package de.rogallab.mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rogallab.mobile.Globals
import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.local.dtos.ArticleRoomDto
import de.rogallab.mobile.data.remote.dtos.ArticleDto

@Database(
   entities = [ ArticleRoomDto::class ],
   version = Globals.DATABASE_VERSION,
   exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun createArticleDao(): IArticleDao
}
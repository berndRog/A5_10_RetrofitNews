package de.rogallab.mobile.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.models.Article
import de.rogallab.mobile.domain.utilities.logDebug

@Database(entities = arrayOf(Article::class),
          version = AppStart.database_version,
          exportSchema = false)
@TypeConverters(Converters::class)

abstract class AppDatabase: RoomDatabase() {

    abstract fun createArticleDao(): IArticleDao

    init{
       logDebug("==>AppDatabase     .","init{${this.hashCode()}}")
    }
}
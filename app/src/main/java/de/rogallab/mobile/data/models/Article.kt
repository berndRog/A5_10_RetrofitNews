package de.rogallab.mobile.data.models

import androidx.room.*

@Entity(tableName = "article")
data class Article(
   @PrimaryKey(autoGenerate = true)
   @ColumnInfo(name = "pk_article")
   val id: Int? = null,
   val author: String = "",
   val content: String? = "",
   val description: String? = "",
   val publishedAt: String = "",
   val source: Source?,
   val title: String = "",
   val url: String = "",
   val urlToImage: String? = ""
)
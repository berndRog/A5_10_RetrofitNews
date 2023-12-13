package de.rogallab.mobile.domain

import de.rogallab.mobile.data.models.Article
import de.rogallab.mobile.data.models.News
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface INewsRepository {
   suspend fun getHeadlines(country: String, page: Int): Response<News>
   suspend fun getEverything(searchText: String, page: Int): Response<News>
   fun selectArticles(): Flow<List<Article>>
   suspend fun upsert(article: Article): Boolean
   suspend fun delete(article: Article): Boolean
}
package de.rogallab.mobile.domain

import androidx.paging.PagingData
import de.rogallab.mobile.data.remote.dtos.NewsDto
import de.rogallab.mobile.domain.entites.Article
import kotlinx.coroutines.flow.Flow

interface INewsRepository {
   fun getEverything(
      searchText: String,
      pageSize: Int = 20,
      sortBy: String = "publishedAt"
   ): Flow<Result<List<Article>>>

   fun getEverythingPaged(
      searchText: String,
      pageSize: Int = 20,
      sortBy: String = "publishedAt"
   ): Flow<PagingData<Article>>

}


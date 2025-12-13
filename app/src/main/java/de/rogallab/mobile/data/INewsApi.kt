package de.rogallab.mobile.data

import de.rogallab.mobile.data.remote.dtos.NewsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface INewsApi {

   // BaseUrl https://newsapi.org/
   @GET("v2/everything")
   suspend fun getEverything(
      @Query("q")          text: String,
      @Query("page")       page: Int = 1,
      @Query("pageSize")   pageSize: Int = 20,
      @Query("sortBy")     sortBy: String = "publishedAt"
   ): Response<NewsDto>
}
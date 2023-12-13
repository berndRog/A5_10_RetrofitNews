package de.rogallab.mobile.data

import de.rogallab.mobile.data.models.News
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface INewsWebservice {

   // BaseUrl https://newsapi.org/

   @GET("v2/top-headlines")
   suspend fun getHeadlines(
      @Query("country")
      country: String = "de",
      @Query("page")
      page: Int = 1
   ): Response<News>


   @GET("v2/everything")
   suspend fun getEverything(
      @Query("q")
      text: String,
      @Query("page")
      page: Int = 1
   ): Response<News>


}
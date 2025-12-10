package de.rogallab.mobile.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(
   private val _keyProvider: () -> String?,
   private val _mode: ApiKeyMode = ApiKeyMode.HEADER,
   private val _headerName: String = "X-API-Key",
   private val _queryName: String = "apiKey"
) : Interceptor {

   override fun intercept(chain: Interceptor.Chain): Response {
      val original = chain.request()

      // If no API key is available, continue request unchanged
      val apiKey = _keyProvider()?.takeIf { it.isNotBlank() }
         ?: return chain.proceed(original)

      return when (_mode) {

         ApiKeyMode.HEADER -> {
            // Attach API key header to the outgoing request
            val modified = original.newBuilder()
               .header(_headerName, apiKey)
               .build()
            chain.proceed(modified)
         }

         ApiKeyMode.QUERY -> {
            // Attach API key as a query parameter to the outgoing request
            val originalUrl = original.url
            val newUrl = originalUrl.newBuilder()
               .addQueryParameter(_queryName, apiKey)
               .build()
            val modified = original.newBuilder()
               .url(newUrl)
               .build()
            chain.proceed(modified)
         }
      }
   }
}

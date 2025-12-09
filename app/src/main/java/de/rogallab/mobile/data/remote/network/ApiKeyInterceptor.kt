package de.rogallab.mobile.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(
   private val _keyProvider: () -> String?      // API key is supplied dynamically
) : Interceptor {

   override fun intercept(chain: Interceptor.Chain): Response {
      val original = chain.request()

      // If no API key is available, continue request unchanged
      val apiKey = _keyProvider()?.takeIf { it.isNotBlank() }
         ?: return chain.proceed(original)

      // Attach API key header to the outgoing request
      val modifiedRequest = original.newBuilder()
         .header("X-API-Key", apiKey)              // custom API key header
         // .header("X-Session", getServerSession())  // optional session if needed
         .build()

      return chain.proceed(modifiedRequest)
   }
}

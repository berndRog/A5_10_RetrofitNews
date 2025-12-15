package de.rogallab.mobile.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response

class BearerTokenInterceptor(
   private val _tokenProvider: () -> String?        // Token is fetched dynamically
) : Interceptor {

   override fun intercept(chain: Interceptor.Chain): Response {
      val original = chain.request()

      // Skip authentication when No-Authentication header is present
      if (original.header(HEADER_NO_AUTH) != null) {
         val cleanRequest = original.newBuilder()
            .removeHeader(HEADER_NO_AUTH)          // remove internal marker header
            .build()
         return chain.proceed(cleanRequest)
      }

      // Get current token
      val token = _tokenProvider()
         ?: return chain.proceed(original)

      // Add Authorization header
      val authenticatedRequest = original.newBuilder()
         .addHeader("Authorization", "Bearer $token")
         .build()

      return chain.proceed(authenticatedRequest)
   }

   companion object {
      private const val HEADER_NO_AUTH = "No-Authentication"
   }
}

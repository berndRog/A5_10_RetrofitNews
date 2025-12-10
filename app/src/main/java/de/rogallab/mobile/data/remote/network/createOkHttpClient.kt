package de.rogallab.mobile.data.remote.network

import de.rogallab.mobile.domain.utilities.logDebug
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

fun createOkHttpClient(
   connectivityInterceptor: ConnectivityInterceptor,
   apiKeyInterceptor: ApiKeyInterceptor,
   bearerTokenInterceptor: BearerTokenInterceptor,
   loggingInterceptor: HttpLoggingInterceptor
) : OkHttpClient {
   logDebug("<-OkHttpClient", "create()")
   return OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(5, TimeUnit.SECONDS)
      .writeTimeout(5, TimeUnit.SECONDS)
      .addInterceptor(connectivityInterceptor)
      .addInterceptor(apiKeyInterceptor)
      .addInterceptor(bearerTokenInterceptor)
      .addInterceptor(loggingInterceptor)
      .build()
}

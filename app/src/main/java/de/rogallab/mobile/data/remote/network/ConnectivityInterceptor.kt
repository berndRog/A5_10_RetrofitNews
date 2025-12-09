package de.rogallab.mobile.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ConnectivityInterceptor(
   private val _networkConnection: NetworkConnection
) : Interceptor {

   override fun intercept(chain: Interceptor.Chain): Response {
      if (!_networkConnection.isOnline())
         throw IOException("Cellular and WiFi are not connected")
      return chain.proceed(chain.request())
   }
}
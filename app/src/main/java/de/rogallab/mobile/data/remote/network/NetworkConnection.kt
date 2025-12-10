package de.rogallab.mobile.data.remote.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkConnection(
   context: Context
): INetworkConnection {
   private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

   override fun isOnline(): Boolean {
      val network = connectivityManager.activeNetwork ?: return false
      val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
      // has internet capability and a transport that supports internet
      val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      // either wifi or cellular
      val hasTransport = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
         capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
      return hasInternet && hasTransport
   }
}
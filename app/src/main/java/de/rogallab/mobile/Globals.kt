package de.rogallab.mobile

import androidx.compose.material3.SnackbarDuration

object Globals {
   const val PAGING = true

   const val BASE_URL: String = "https://newsapi.org/"
   const val API_KEY:  String = BuildConfig.NEWS_API_KEY
   const val BEARER_TOKEN:  String = ""

   const val DATABASE_NAME = "A5_10_RetrofitNews.db"
   const val DATABASE_VERSION  = 1

   const val fileName = DATABASE_NAME
   const val directoryName = "android"

   val mediaStoreGroupname = "Retrofit news"

   val animationDuration = 1000
   val snackbarDuration = SnackbarDuration.Indefinite

   var isDebug = true
   var isInfo = true
   var isVerbose = true
   var isComp = false
}

class BearerTokenStore {
   var token: String?
      get() = BuildConfig.BEARER_TOKEN.takeIf { it.isNotBlank() }
      set(_) {}
}


class ApiKeyStore {
   var apiKey: String?
      get() = BuildConfig.NEWS_API_KEY.takeIf { it.isNotBlank() }
      set(_) {} // ignore for now (later when dynamic updates needed)
}
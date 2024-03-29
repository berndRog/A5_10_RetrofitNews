package de.rogallab.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.rogallab.mobile.domain.utilities.logInfo

@HiltAndroidApp
class AppStart : Application() {
   override fun onCreate() {
      super.onCreate()

      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(tag, "onCreate() maxMemory $maxMemory kB")
   }

   companion object {
      //                       12345678901234567890123
      private const val tag = "ok>AppStart           ."
      const val isInfo = true
      const val isDebug = true
      const val database_name:    String = "A5_10_RetrofitNews.db"
      const val database_version: Int    = 1
      const val base_url: String = "https://newsapi.org/"
      const val api_key:  String = "a904cda52f054306a6cc9a3494b36aad"
      const val bearer_token:  String = ""
      const val ARTICLE_SEARCH_TIME_DELAY = 1000L
   }
}
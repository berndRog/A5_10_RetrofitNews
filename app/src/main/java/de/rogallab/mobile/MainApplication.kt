package de.rogallab.mobile

import android.app.Application
import android.util.Log
import de.rogallab.mobile.di.defModules
import de.rogallab.mobile.domain.utilities.LogConfig
import de.rogallab.mobile.domain.utilities.compLogger
import de.rogallab.mobile.domain.utilities.debugLogger
import de.rogallab.mobile.domain.utilities.errorLogger
import de.rogallab.mobile.domain.utilities.infoLogger
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.verboseLogger
import de.rogallab.mobile.domain.utilities.warningLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {

   override fun onCreate() {
      super.onCreate()

      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(TAG, "onCreate() maxMemory $maxMemory kB")

      logInfo(TAG, "onCreate(): startKoin{...}")
      startKoin {
         // Log Koin into Android logger
         androidLogger(Level.DEBUG)
         // Reference Android context
         androidContext(this@MainApplication)
         // Load modules
         modules(defModules)
      }

      AndroidLoggerInitializer.init(
         isInfo = Globals.isInfo,
         isDebug = Globals.isDebug,
         isVerbose = Globals.isVerbose,
         isComp = Globals.isComp
      )
   }

   companion object {
      private const val TAG = "<-AppStart"
   }
}

object AndroidLoggerInitializer {
   fun init(
      isInfo: Boolean = Globals.isInfo,
      isDebug: Boolean = Globals.isDebug,
      isVerbose: Boolean = Globals.isVerbose,
      isComp: Boolean = Globals.isComp
   ) {
      LogConfig.isInfo = isInfo
      LogConfig.isDebug = isDebug
      LogConfig.isVerbose = isVerbose
      LogConfig.isComp = isComp

      errorLogger = { tag, msg -> Log.e(tag, msg) }
      warningLogger = { tag, msg -> Log.w(tag, msg) }
      infoLogger = { tag, msg -> Log.i(tag, msg) }
      debugLogger = { tag, msg -> Log.d(tag, msg) }
      verboseLogger = { tag, msg -> Log.v(tag, msg) }
      compLogger = { tag, msg -> Log.d(tag, msg) }
   }
}
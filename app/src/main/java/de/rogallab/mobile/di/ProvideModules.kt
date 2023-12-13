package de.rogallab.mobile.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.INewsWebservice
import de.rogallab.mobile.data.database.AppDatabase
import de.rogallab.mobile.data.network.ApiKey
import de.rogallab.mobile.data.network.BearerToken
import de.rogallab.mobile.data.network.NetworkConnection
import de.rogallab.mobile.data.network.NetworkConnectivity
import de.rogallab.mobile.data.network.WebserviceBuilder
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(ViewModelComponent::class)
object ProvideModules {
                          //12345678901234567890123
   private const val tag = "ok>AppProvidesModules ."

   @Provides
   @ViewModelScoped
   fun provideContext(
      application: Application // provided by Hilt
   ): Context {
      logInfo(tag,"provideContext()")
      return application.applicationContext
   }

   @Provides
   @ViewModelScoped
   fun provideCoroutineExceptionHandler(
   ): CoroutineExceptionHandler {
      logInfo(tag, "providesCoroutineExceptionHandler()")
      return CoroutineExceptionHandler { _, exception ->
         exception.localizedMessage?.let {
            logError("ok>CoroutineException", it)
         } ?: run {
            exception.stackTrace.forEach {
               logError("ok>CoroutineException", it.toString())
            }
         }
      }
   }

   @Provides
   @ViewModelScoped
   fun provideDispatcher(): CoroutineDispatcher {
      logInfo(tag,"provideDispatcher()")
      return Dispatchers.IO
   }

   @Provides
   @ViewModelScoped
   fun provideAppDatabase(
      application: Application // provided by Hilt
   ): AppDatabase {
      logInfo(tag,"provideAppDatabase()")
      return Room.databaseBuilder(
         application.applicationContext,
         AppDatabase::class.java,
         AppStart.database_name
      ).fallbackToDestructiveMigration()
         .build()
   }

   @Provides
   @ViewModelScoped
   fun provideArticleDao(
      database: AppDatabase
   ): IArticleDao {
      logInfo(tag,"provideArticleDao()")
      return database.createArticleDao()
   }

   @Provides
   @ViewModelScoped
   fun provideNetworkConnection(
      context: Context
   ): NetworkConnection {
      logInfo(tag,"provideNetworkConnection()")
      return NetworkConnection(context)
   }

   @Provides
   @ViewModelScoped
   fun provideNetworkConnectivityInterceptor(
      networkConnection: NetworkConnection
   ): NetworkConnectivity {
      logInfo(tag,"provideNetworkConnectivityInterceptor()")
      return NetworkConnectivity(networkConnection)
   }

   @Provides
   @ViewModelScoped
   fun provideApiKeyInterceptor(
   ): ApiKey {
      logInfo(tag,"provideApiKeyInterceptor()")
      return ApiKey()
   }

   @Provides
   @ViewModelScoped
   fun provideBearerTokenInterceptor(
   ): BearerToken {
      logInfo(tag,"provideBearerTokenInterceptor()")
      return BearerToken()
   }


   @Provides
   @ViewModelScoped
   fun provideNewsWebservice(
      networkConnectivity: NetworkConnectivity,
      apiKey: ApiKey,
      bearerToken: BearerToken
   ): INewsWebservice {
      logInfo(tag,"provideNewsWebservice()")
      WebserviceBuilder(
         networkConnectivity,
         apiKey,
         bearerToken
      ).apply{
         return create(INewsWebservice::class.java, "NewsWebservice")
      }
   }
}
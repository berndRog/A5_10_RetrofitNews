package de.rogallab.mobile.test.di

import android.content.Context
import androidx.navigation3.runtime.NavKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import coil.ImageLoader
import de.rogallab.mobile.ApiKeyStore
import de.rogallab.mobile.BearerTokenStore
import de.rogallab.mobile.createImageLoader
import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.INewsApi
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.remote.network.ApiKeyInterceptor
import de.rogallab.mobile.data.remote.network.ApiKeyMode
import de.rogallab.mobile.data.remote.network.BearerTokenInterceptor
import de.rogallab.mobile.data.remote.network.ConnectivityInterceptor
import de.rogallab.mobile.data.remote.network.INetworkConnection
import de.rogallab.mobile.data.remote.network.createOkHttpClient
import de.rogallab.mobile.data.remote.network.createRetrofit
import de.rogallab.mobile.data.remote.network.createWebservice
import de.rogallab.mobile.data.repositories.ArticleRepository
import de.rogallab.mobile.data.repositories.NewsRepository
import de.rogallab.mobile.domain.IArticleRepository
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.test.data.SeedTestdata
import de.rogallab.mobile.ui.features.article.ArticlesViewModel
import de.rogallab.mobile.ui.features.news.NewsBaseViewModel
import de.rogallab.mobile.ui.features.news.NewsViewModel
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.Nav3ViewModelTopLevel
import kotlinx.coroutines.CoroutineDispatcher
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit


private val DISPATCHER_IO = named("dispatcherIo")

fun defModulesTest(
   mockWebServer: MockWebServer,
   ioDispatcher: CoroutineDispatcher
): Module = module {
   val tag = "<-defModulesTest"


   logInfo(tag, "test single    -> ApplicationProvider.getApplicationContext()")
   single<Context> {
      ApplicationProvider.getApplicationContext()
   }

   // data modules Dispatcher
   logInfo(tag, "test single    -> ioDispatcher: CoroutineDispatcher")
   single<CoroutineDispatcher>(DISPATCHER_IO) {
      ioDispatcher  // testDispatcher via paremter
   }

   logInfo(tag, "test single    -> SeedTestdata")
   single<SeedTestdata> {
      SeedTestdata()
   }


   //== data modules Room ==========================================================================
   logInfo(tag, "test single    -> AppDatabase")
   single<AppDatabase> {
      Room.inMemoryDatabaseBuilder(
         context = get<Context>(),
         klass = AppDatabase::class.java,
      ).allowMainThreadQueries()
         .build()
   }

   logInfo(tag, "test single    -> IPersonDao")
   single<IArticleDao> {
      get<AppDatabase>().createArticleDao()
   }

   logInfo(tag, "test single    -> PersonRepository: IPersonRepository")
   single<IArticleRepository> {
      ArticleRepository(
         _articleDao = get<IArticleDao>(),
         _dispatcher = get(DISPATCHER_IO),
      )
   }

   //== data modules Retrofit / MockWebServer ======================================================
   logInfo(tag, "test single    -> MockWebServer")
   single { mockWebServer }


   logInfo(tag, "test single    -> NetworkConnection")
   class NetworkConnectionMock(context:Context): INetworkConnection {
      override fun isOnline(): Boolean = true
   }
   single<INetworkConnection> {
      NetworkConnectionMock(context = get<Context>())
   }

   logInfo(tag, "test single    -> ConnectivityInterceptor")
   single<ConnectivityInterceptor> {
      ConnectivityInterceptor(
         _networkConnection = get<INetworkConnection>()
      )
   }

   logInfo(tag, "test single    -> ApiKeyStore & BearerTokenStore")
   single { ApiKeyStore() }
   single { BearerTokenStore() }

   logInfo(tag, "test single    -> InterceptorApiKey")
   single<ApiKeyInterceptor> {
      ApiKeyInterceptor(
         _keyProvider = { get<ApiKeyStore>().apiKey },
         _mode = ApiKeyMode.HEADER,
         _headerName = "X-API-Key",
         _queryName = "apiKey"
      )
   }
   logInfo(tag, "test single    -> InterceptorBearerToken")
   single<BearerTokenInterceptor> {
      BearerTokenInterceptor(
         _tokenProvider = { get<BearerTokenStore>().token }
      )
   }

   logInfo(tag, "test single    -> HttpLoggingInterceptor")
   single<HttpLoggingInterceptor> {
      HttpLoggingInterceptor().apply {
         level = HttpLoggingInterceptor.Level.BODY
      }
   }

   logInfo(tag, "test single    -> OkHttpClient")
   single<OkHttpClient> {
      createOkHttpClient(
         connectivityInterceptor = get<ConnectivityInterceptor>(),
         apiKeyInterceptor = get<ApiKeyInterceptor>(),
         bearerTokenInterceptor = get<BearerTokenInterceptor>(),
         loggingInterceptor = get<HttpLoggingInterceptor>(),
      )
   }

   logInfo(tag, "test single    -> Retrofit")
   single<Retrofit> {
      createRetrofit(
         baseUrl = mockWebServer.url("/").toString(),
         okHttpClient = get<OkHttpClient>()
      )
   }

   logInfo(tag, "test single    -> NewsApiService: INewsApi")
   single<INewsApi> {
      createWebservice<INewsApi>(
         get<Retrofit>(),
         "NewsApiService"  // implementation of INewsApi
      )
   }

   logInfo(tag, "test single    -> PersonRepository: IPersonRepository")
   single<INewsRepository> {
      NewsRepository(
         _newsApi = get<INewsApi>(),
         _dispatcher = get<CoroutineDispatcher>(DISPATCHER_IO)
      )
   }
   //== domain modules =============================================================================

   //== ui modules =================================================================================
   logInfo(tag, "test single    -> createImageLoader")
   single<ImageLoader> { createImageLoader(androidContext()) }

   logInfo(tag, "test factory -> Nav3ViewModel as INavHandler (with params)")
   factory { (startDestination: NavKey) ->  // Parameter for startDestination
      Nav3ViewModelTopLevel(startDestination = startDestination)
   } bind INavHandler::class


   logInfo(tag, "test factory -> NewsViewModel")
   factory { (navHandler: INavHandler) ->
      NewsViewModel(
         repository = get<INewsRepository>(),
         imageLoader = get<ImageLoader>(),
         navHandler = navHandler,
      )
   }
   logInfo(tag, "test factory -> ArticlesViewModel")
   viewModel<ArticlesViewModel> { (navHandler: INavHandler) ->
      ArticlesViewModel(
         _repository = get<IArticleRepository>(),
         _navHandler = navHandler,
      )
   }
}
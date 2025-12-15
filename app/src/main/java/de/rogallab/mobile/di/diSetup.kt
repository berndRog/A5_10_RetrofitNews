package de.rogallab.mobile.di

import androidx.navigation3.runtime.NavKey
import androidx.room.Room
import coil.ImageLoader
import de.rogallab.mobile.ApiKeyStore
import de.rogallab.mobile.BearerTokenStore
import de.rogallab.mobile.Globals
import de.rogallab.mobile.createImageLoader
import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.INewsApi
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.remote.network.ApiKeyInterceptor
import de.rogallab.mobile.data.remote.network.BearerTokenInterceptor
import de.rogallab.mobile.data.remote.network.ConnectivityInterceptor
import de.rogallab.mobile.data.remote.network.INetworkConnection
import de.rogallab.mobile.data.remote.network.NetworkConnection
import de.rogallab.mobile.data.remote.network.createOkHttpClient
import de.rogallab.mobile.data.remote.network.createRetrofit
import de.rogallab.mobile.data.remote.network.createWebservice
import de.rogallab.mobile.data.repositories.ArticleRepository
import de.rogallab.mobile.data.repositories.NewsRepository
import de.rogallab.mobile.domain.IArticleRepository
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.features.article.ArticlesViewModel
import de.rogallab.mobile.ui.features.news.NewsBaseViewModel
import de.rogallab.mobile.ui.features.news.NewsPagingViewModel
import de.rogallab.mobile.ui.features.news.NewsViewModel
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.Nav3ViewModelTopLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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


val defModules: Module = module {
   val tag = "<-defModules"

   // Provide Dispatchers
   logInfo(tag, "single    -> DispatcherIo:CoroutineDispatcher")
   single<CoroutineDispatcher>(DISPATCHER_IO) { Dispatchers.IO }

   //== data modules ===============================================================================
   // local Room Database -----------------------------------------------------
   logInfo(tag, "single    -> AppDatabase")
   single<AppDatabase> {
      Room.databaseBuilder(
         context = androidContext(),
         klass = AppDatabase::class.java,
         name = Globals.DATABASE_NAME
      ).build()
   }
   logInfo(tag, "single    -> IArticleDao")
   single<IArticleDao> { get<AppDatabase>().createArticleDao() }

   logInfo(tag, "single    -> ArticleRepository: IArticleRepository")
   single<IArticleRepository> {
      ArticleRepository(
         _articleDao = get<IArticleDao>(),
         _dispatcher = get<CoroutineDispatcher>(DISPATCHER_IO),
      )
   }

   // remote OkHttp/Retrofit Webservice ---------------------------------------
   logInfo(tag, "single    -> NetworkConnection")
   single<INetworkConnection> {
      NetworkConnection(context = androidContext())
   }

   logInfo(tag, "single    -> ConnectivityInterceptor")
   single<ConnectivityInterceptor> {
      ConnectivityInterceptor(
         _networkConnection = get<INetworkConnection>()
      )
   }

   single { BearerTokenStore() }   // holds bearer token
   single { ApiKeyStore() }        // holds API key
   logInfo(tag, "single    -> InterceptorApiKey")
   single<ApiKeyInterceptor> {
      ApiKeyInterceptor(
         _keyProvider = { get<ApiKeyStore>().apiKey }
      )
   }
   logInfo(tag, "single    -> InterceptorBearerToken")
   single<BearerTokenInterceptor> {
      BearerTokenInterceptor(
         _tokenProvider = { get<BearerTokenStore>().token }
      )
   }

   logInfo(tag, "single    -> HttpLoggingInterceptor")
   single<HttpLoggingInterceptor> {
      HttpLoggingInterceptor().apply {
         level = HttpLoggingInterceptor.Level.BODY
      }
   }

   logInfo(tag, "single    -> OkHttpClient")
   single<OkHttpClient> {
      createOkHttpClient(
         connectivityInterceptor = get<ConnectivityInterceptor>(),
         apiKeyInterceptor = get<ApiKeyInterceptor>(),
         bearerTokenInterceptor = get<BearerTokenInterceptor>(),
         loggingInterceptor = get<HttpLoggingInterceptor>(),
      )
   }
   logInfo(tag, "single    -> Retrofit")
   single<Retrofit> {
      createRetrofit(
         baseUrl = Globals.BASE_URL,
         okHttpClient = get<OkHttpClient>()
      )
   }
   logInfo(tag, "single    -> NewsApiService: INewsApi")
   single<INewsApi> {
      createWebservice<INewsApi>(
         get<Retrofit>(),
         "NewsApiService"  // implementation of INewsApi
      )
   }

   // Provide IPersonRepository`
   logInfo(tag, "single    -> NewsRepository: INewsRepository")
   single<INewsRepository> {
      NewsRepository(
         _newsApi = get<INewsApi>(),
         _dispatcher = get<CoroutineDispatcher>(DISPATCHER_IO)
      )
   }

   //== ui modules =================================================================================
   logInfo(tag, "single    -> createImageLoader")
   single<ImageLoader> { createImageLoader(androidContext()) }

   logInfo(tag, "viewModel -> Nav3ViewModelTopLevel as INavHandler (with params)")
   viewModel<Nav3ViewModelTopLevel> { (startDestination: NavKey) ->  // Parameter for startDestination
      Nav3ViewModelTopLevel(startDestination = startDestination)
   } bind INavHandler::class

   logInfo(tag, "viewModel -> NewsViewModel")
   viewModel<NewsViewModel> { (navHandler: INavHandler) ->
      NewsViewModel(
         repository = get<INewsRepository>(),
         imageLoader = get<ImageLoader>(),
         navHandler = navHandler
      )
   }
   logInfo(tag, "viewModel -> NewsPagingViewModel")
   viewModel<NewsPagingViewModel> { (navHandler: INavHandler) ->
      NewsPagingViewModel(
         repository = get<INewsRepository>(),
         imageLoader = get<ImageLoader>(),
         navHandler = navHandler
      )
   }

   logInfo(tag, "viewModel -> ArticlesViewModel")
   viewModel<ArticlesViewModel> { (navHandler: INavHandler) ->
      ArticlesViewModel(
         _repository = get<IArticleRepository>(),
         _navHandler = navHandler,
      )
   }

}

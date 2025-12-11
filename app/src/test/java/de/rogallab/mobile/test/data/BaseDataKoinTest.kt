package de.rogallab.mobile.test.data

import androidx.test.core.app.ApplicationProvider
import de.rogallab.mobile.Globals
import de.rogallab.mobile.test.MainDispatcherRule
import de.rogallab.mobile.test.TestApplication
import de.rogallab.mobile.test.di.defModulesTest
import de.rogallab.mobile.test.setupConsoleLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// package de.rogallab.mobile.test.base

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseDataKoinTest : KoinTest {

   @get:Rule
   val mainDispatcherRule = MainDispatcherRule()

   protected lateinit var _mockWebServer: MockWebServer
   protected lateinit var _seed: SeedTestdata
   protected lateinit var _koin: Koin

   @Before
   fun baseSetUp() {
      // Logging
      Globals.isInfo = true
      Globals.isDebug = true
      Globals.isVerbose = false
      Globals.isComp = false
      setupConsoleLogger()

      // Ensure clean Koin context
      stopKoin()

      // Start MockWebServer
      _mockWebServer = MockWebServer().apply { start() }

      // Start Koin with test module
      val testModule = defModulesTest(
         mockWebServer = _mockWebServer,
         ioDispatcher = mainDispatcherRule.dispatcher()
      )
      val koinApp = GlobalContext.startKoin {
         // for ImageLoader etc.
         androidContext(ApplicationProvider.getApplicationContext())
         modules(testModule)
      }

      // Common beans available in all tests
      _koin = koinApp.koin
      _seed = _koin.get()
   }

   @After
   fun baseTearDown() {
      _mockWebServer.close()
      stopKoin()
   }
}
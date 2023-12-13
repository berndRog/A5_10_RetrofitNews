package de.rogallab.mobile.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.navigation.AppNavHost
import de.rogallab.mobile.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : BaseActivity(tag) {

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContent {

         AppTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
               AppNavHost( )
            }
         }
      }
   }

   companion object {
      //                       12345678901234567890123
      private const val tag = "ok>MainActivity       ."
   }
}


//
//@Composable
//fun Greeting(name: String) {
//   Text(text = "Hello $name!")
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//   B4_00_FlowTheme {
//      Greeting("Android")
//   }
//}
package de.rogallab.mobile.ui.news

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArticleScreen(
   navController: NavController,
   viewModel: NewsViewModel
) {
            //12345678901234567890
   val tag = "ok>ArticleScreen   ."

   val article = viewModel.article!!

   val snackbarHostState = remember { SnackbarHostState() }
   val coroutineScope = rememberCoroutineScope()

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.readarticle)) },
            navigationIcon = {
               IconButton(onClick = {
                  logInfo(tag, "Reverse Navigation (Up)")
                  navController.navigate(route = NavScreen.Headlines.route) {
                     popUpTo(route = NavScreen.SearchNews.route) { inclusive = true }
                  }
               }) {
                  Icon(imageVector = Icons.Default.ArrowBack,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      floatingActionButton = {
         FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.secondary,
            onClick = {
               // FAB clicked -> InputScreen initialized
               logDebug(tag, "Forward Navigation: FAB clicked")
               viewModel.upsert(article)
               coroutineScope.launch {
                  val snackbarResult = snackbarHostState.showSnackbar(
                     message = "Artikel gespeichert"
                  )
               }
            }
         ) {
            Icon(Icons.Default.Add, "Add a contact")
         }
      },
      bottomBar = {
         AppNavigationBar(navController)
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }) { paddingValues ->

      Column(modifier = Modifier.padding(paddingValues = paddingValues)) {
         AndroidView(
            factory = { context ->
               WebView(context).apply {
               layoutParams = ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
               )
               webViewClient = WebViewClient()
               settings.loadWithOverviewMode = true
               settings.javaScriptEnabled = true
               loadUrl(article.url)
            }
         }, update = {
            it.loadUrl(article.url)
         })
      }
   }
}
package de.rogallab.mobile.ui.news

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen

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

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.searchnews)) },
            navigationIcon = {
               IconButton(onClick = {
                  logInfo(tag, "Reverse Navigation (Up)")
                  navController.navigate(route = NavScreen.Headlines.route) {
                     popUpTo(route = NavScreen.Headlines.route) { inclusive = true }
                  }
               }) {
                  Icon(imageVector = Icons.Default.ArrowBack,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
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

         FilledTonalButton(
            modifier = Modifier
               .padding(horizontal = 0.dp)
               .padding( bottom=8.dp).fillMaxWidth(),
            onClick = {
               viewModel.upsert(article)
            }
         ) {
            Text(text = "Speichern")
         }
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

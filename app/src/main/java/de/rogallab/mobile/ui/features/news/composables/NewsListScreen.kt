package de.rogallab.mobile.ui.features.news.composables

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logComp
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.composables.CollectBy
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.features.article.ArticleIntent
import de.rogallab.mobile.ui.features.article.ArticlesViewModel
import de.rogallab.mobile.ui.features.article.composables.ArticleItem
import de.rogallab.mobile.ui.features.news.NewsIntent
import de.rogallab.mobile.ui.features.news.NewsBaseViewModel
import de.rogallab.mobile.ui.features.news.NewsViewModel
import de.rogallab.mobile.ui.navigation.Nav3ViewModelTopLevel
import de.rogallab.mobile.ui.navigation.composables.BottomNav3Bar
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
   navViewModel: Nav3ViewModelTopLevel,
   newsViewModel: NewsViewModel,
   articlesViewModel: ArticlesViewModel,
   imageLoader: ImageLoader = koinInject()
) {
   val tag = "<-NewsListScreen"
   val nComp = remember { mutableIntStateOf(1) }
   SideEffect { logComp(tag, "Composition #${nComp.value++}") }

   // Common UI state (searchText, loading, news for legacy)
   val newsUiState = CollectBy(newsViewModel.newsUiStateFlow, tag)

   val snackbarHostState = remember { SnackbarHostState() }
   Scaffold(
      contentColor = MaterialTheme.colorScheme.onBackground,
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.searchnews)) },
            navigationIcon = {
               val activity: Activity? = LocalActivity.current
               IconButton(onClick = { activity?.finish() }) {
                  Icon(
                     imageVector = Icons.Default.Menu,
                     contentDescription = stringResource(R.string.back)
                  )
               }
            }
         )
      },
      bottomBar = {
         BottomNav3Bar(navViewModel)
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data)
         }
      },
      modifier = Modifier.fillMaxSize()
   ) { paddingValues ->

      Column(
         modifier = Modifier
            .padding(paddingValues = paddingValues)
            .padding(horizontal = 16.dp)
      ) {

         // --- Search Bar ---
         SearchField(
            searchText = newsUiState.searchText,
            onSearchTextChange = { it ->
               newsViewModel.onProcessIntent(NewsIntent.SearchTextChange(it))
            },
            onTriggerSearch = {
               newsViewModel.onProcessIntent(NewsIntent.Reload)
            }
         )
         // --- Loading indicator ---
         if (newsUiState.loading) {
            SideEffect { logDebug(tag, "loading indicator") }
            Column(
               modifier = Modifier.fillMaxSize(),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
            ) {
               CircularProgressIndicator()
            }
         } else if (newsUiState.articles.isNotEmpty()) {

            SideEffect { logDebug(tag, "loaded:${newsUiState.articles.size}") }
            val articles = newsUiState.articles ?: emptyList()
            LazyColumn(
               modifier = Modifier.fillMaxSize(),
               state = rememberLazyListState()
            ) {
               items(
                  items = articles,
                  key = { it.id!! }
               ) { article ->
                  ArticleItem(
                     article = article,
                     onClick = {
                        articlesViewModel.onProcessIntent(
                           ArticleIntent.ShowWebArticle(isNews = true, article = article))
                     },
                     imageLoader = imageLoader
                  )
               }
            }
         }
      } // Column
   } // Scaffold

   ErrorHandler(
      viewModel = newsViewModel,
      snackbarHostState = snackbarHostState
   )
}
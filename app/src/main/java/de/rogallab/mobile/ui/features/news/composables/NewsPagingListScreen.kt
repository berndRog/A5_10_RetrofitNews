package de.rogallab.mobile.ui.features.news.composables

import android.R.attr.onClick
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.composables.CollectBy
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.features.article.ArticleIntent
import de.rogallab.mobile.ui.features.article.ArticlesViewModel
import de.rogallab.mobile.ui.features.article.composables.ArticleItem
import de.rogallab.mobile.ui.features.news.NewsIntent
import de.rogallab.mobile.ui.features.news.NewsPagingViewModel
import de.rogallab.mobile.ui.features.news.NewsViewModel
import de.rogallab.mobile.ui.navigation.Nav3ViewModelTopLevel
import de.rogallab.mobile.ui.navigation.composables.BottomNav3Bar
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsPagingListScreen(
   navViewModel: Nav3ViewModelTopLevel,
   newsPagingViewModel: NewsPagingViewModel,
   articlesViewModel: ArticlesViewModel,
   imageLoader: ImageLoader = koinInject()
) {
   val tag = "<-NewsPagingListScreen"

   // Common UI state (searchText)
   val newsUiState = CollectBy(newsPagingViewModel.newsUiStateFlow, tag)

   // Paging items
   val pagingItems = newsPagingViewModel.pagedArticlesFlow.collectAsLazyPagingItems()
   val refreshState = pagingItems.loadState.refresh
   val appendState = pagingItems.loadState.append

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
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
      ) {
         // --- Search Bar ---
         SearchField(
            searchText = newsUiState.searchText,
            onSearchTextChange = {
               newsPagingViewModel.onProcessIntent(NewsIntent.SearchTextChange(it))
            },
            onTriggerSearch = {
               newsPagingViewModel.onProcessIntent(NewsIntent.Reload)
            }
         )

         SideEffect { logDebug(tag, "paging itemCount=${pagingItems.itemCount}") }

         // Show fullscreen loading ONLY for initial load with no items
         val showInitialLoading =
            pagingItems.itemCount == 0 && refreshState is LoadState.Loading

         if (showInitialLoading) {
            Column(
               modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
            ) {
               CircularProgressIndicator()
            }
         } else {
            LazyColumn(
               modifier = Modifier.fillMaxSize(),
               state = rememberLazyListState()
            ) {
               items(
                  count = pagingItems.itemCount,
                  key = pagingItems.itemKey {  article -> article.id }
               ) { index ->
                  pagingItems[index]?.let { article ->
                     ArticleItem(
                        article = article,
                        onClick = {
                           articlesViewModel.onProcessIntent(
                              ArticleIntent.ShowWebArticle(isNews = true, article = article)
                           )
                        },
                        imageLoader = imageLoader
                     )
                  }
               }

               // Footer: append loading / error (optional)
               item {
                  when (pagingItems.loadState.append) {
                     is LoadState.Loading -> {
                        Column(
                           modifier = Modifier
                              .fillMaxSize()
                              .padding(16.dp),
                           horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                           CircularProgressIndicator()
                        }
                     }
                     else -> Unit
                  }
               }
            }
         }
      }
   }

   // Report paging errors as a side effect (NOT during composition)

   if (refreshState is LoadState.Error) {
      LaunchedEffect(refreshState.error) {
         newsPagingViewModel.onProcessIntent(
            NewsIntent.HandleError(refreshState.error)
         )
      }
   }

   // Report paging errors as a side effect after first reloading
   if (appendState is LoadState.Error) {
      LaunchedEffect(appendState.error) {
         newsPagingViewModel.onProcessIntent(
            NewsIntent.HandleError( appendState.error)
         )
      }
   }


   ErrorHandler(
      viewModel = newsPagingViewModel,
      snackbarHostState = snackbarHostState
   )
}
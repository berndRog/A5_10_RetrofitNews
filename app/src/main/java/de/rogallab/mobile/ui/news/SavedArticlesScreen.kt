package de.rogallab.mobile.ui.news

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.android.ui.news.NewsItem
import de.rogallab.mobile.R
import de.rogallab.mobile.data.models.Article
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.composables.LogUiStates
import de.rogallab.mobile.ui.composables.SetSwipeBackgroud
import de.rogallab.mobile.ui.composables.showErrorMessage
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.news.composables.HandleUiStateError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun SavedArticlesScreen(
   viewModel: NewsViewModel,
   navController: NavController
) {
            //12345678901234567890123
   val tag = "ok>SavedArticlesScreen."

   val stateFlowArticles by viewModel.stateFlowArticles.collectAsStateWithLifecycle()
   LogUiStates(stateFlowArticles, "UiState List<Article>", tag)

   val stateError by viewModel.stateFlowError.collectAsStateWithLifecycle()
   LogUiStates(stateError, "UiState Error", tag)

//   LaunchedEffect(Unit) {
//      logDebug(tag, "readSavedArticles()")
//      viewModel.readSavedArticles()
//   }

   val snackbarHostState = remember { SnackbarHostState() }
   val coroutineScope = rememberCoroutineScope()

   BackHandler(
      enabled = true,
      onBack = {
         logInfo(tag, "Back Navigation (Abort)")
         navController.popBackStack(
            route = NavScreen.Headlines.route,
            inclusive = false
         )
      }
   )

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.savedarticles)) },
            navigationIcon = {
               IconButton(onClick = {
                  logInfo(tag, "Reverse Navigation (Up)")
                  navController.navigate(route = NavScreen.Headlines.route) {
                     popUpTo(route = NavScreen.Headlines.route) { inclusive = true }
                  }
               }) {
                  Icon(
                     imageVector = Icons.Default.ArrowBack,
                     contentDescription = stringResource(R.string.back)
                  )
               }
            }
         )
      },
      bottomBar = {
         AppNavigationBar(navController = navController)
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }
   ) { paddingValues ->

      // Icon(imageVector = Icons.Filled.Search)
      Column(
         modifier = Modifier.padding(paddingValues = paddingValues)
      ) {

         if (stateFlowArticles == UiState.Empty) {
            logDebug(tag, "stateFlowArticles.Empty")
            // nothing to do
         } else if (stateFlowArticles == UiState.Loading) {
            logDebug(tag, "stateFlowArticles.Loading")
            Column(
               modifier = Modifier
                  .padding(horizontal = 8.dp)
                  .fillMaxSize(),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
            ) {
               CircularProgressIndicator(modifier = Modifier.size(160.dp))
            }

         } else if (
            stateFlowArticles is UiState.Success<List<Article>> ||
            stateFlowArticles is UiState.Error ||
            stateError is UiState.Error)  {

            if(stateFlowArticles is UiState.Success) {
               (stateFlowArticles as UiState.Success).data?.let { articles: List<Article> ->
                  logDebug(tag, "savedArticles ${articles.size}")

                  LazyColumn(
                     modifier = Modifier
                        .padding(horizontal = 8.dp),
                     state = rememberLazyListState()
                  ) {
                     items(items = articles) { article ->
                        val dismissState = rememberDismissState(
                           confirmValueChange = {
                              if (it == DismissValue.DismissedToEnd) {
                                 logDebug("==>SwipeToDismiss().", "-> Edit")
                                 viewModel.article = article
                                 navController.navigate(NavScreen.Article.route)
                                 return@rememberDismissState true
                              } else if (it == DismissValue.DismissedToStart) {
                                 logDebug("==>SwipeToDismiss().", "-> Delete")
                                 viewModel.delete(article)
                                 val job = coroutineScope.launch {
                                    showErrorMessage(
                                       snackbarHostState = snackbarHostState,
                                       errorMessage = "Wollen Sie das löschen rückgängig machen?",
                                       actionLabel = "ja",
                                       onErrorAction = {
                                          viewModel.upsert(article)
                                       }
                                    )
                                 }
                                 coroutineScope.launch {
                                    job.join()
                                    navController.navigate(NavScreen.SavedArticles.route)
                                 }
                                 return@rememberDismissState true
                              }
                              return@rememberDismissState false
                           }
                        )
                        SwipeToDismiss(
                           state = dismissState,
                           modifier = Modifier.padding(vertical = 4.dp),
                           directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                           background = {
                              SetSwipeBackgroud(dismissState)
                           },
                           dismissContent = {
                              Column(modifier = Modifier.clickable {
                                 viewModel.article = article
                                 navController.navigate(NavScreen.Article.route)
                              }) {
                                 NewsItem(
                                    article,
                                    onClick = { }
                                 )
                              }
                           }
                        )
                     }
                  }
               }
            }
            if(stateFlowArticles is UiState.Error) {
               HandleUiStateError(
                  uiStateFlow = stateFlowArticles,
                  actionLabel = "Ok",
                  onErrorAction = { },
                  snackbarHostState = snackbarHostState,
                  navController = navController,
                  routePopBack = NavScreen.Headlines.route,
                  onUiStateFlowChange = { },
                  tag = tag
               )

            }
         }
      }
   }
}
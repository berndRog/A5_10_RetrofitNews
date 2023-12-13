package de.rogallab.mobile.ui.news

import android.app.Activity
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.data.models.Article
import de.rogallab.mobile.data.models.News
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.composables.LogUiStates
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.news.composables.HandleUiStateError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadLinesScreen(
   viewModel: NewsViewModel,
   navController: NavController
) {
   //12345678901234567890
   val tag = "ok>HeadlinesScreen    ."

   val stateFlowHeadlines by viewModel.stateFlowHeadlines.collectAsStateWithLifecycle()
   LogUiStates(stateFlowHeadlines,"UiState News", tag )

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.headlines)) },
            navigationIcon = {
               val activity = LocalContext.current as Activity
               IconButton(
                  onClick = {
                     logDebug(tag, "Lateral Navigation: finish app")
                     activity.finish()
                  }) {
                  Icon(imageVector = Icons.Default.Menu,
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

      Column(modifier = Modifier
         .padding(paddingValues)
         .padding(horizontal = 8.dp)) {

         when (stateFlowHeadlines) {

            is UiState.Loading -> {
               logDebug(tag, "Loading...")
               Column(
                  modifier = Modifier
                     .padding(paddingValues = paddingValues)
                     .padding(horizontal = 8.dp)
                     .fillMaxSize(),
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally
               ) {
                  CircularProgressIndicator(modifier = Modifier.size(160.dp))
               }
            }

            is UiState.Success -> {
               (stateFlowHeadlines as UiState.Success).data?.let { news: News ->
                  logDebug(tag, "Headlines Success ${news.totalResults}")
                  news.articles.let { articles: List<Article> ->

                     LazyColumn(
                        state = rememberLazyListState()
                     ) {
                        items(articles) { article: Article ->

                           Column(modifier = Modifier
                              .padding()
                              .clickable {
                                 logVerbose(tag,"${article.url}")
                                 viewModel.article = article
                                 navController.navigate(NavScreen.Article.route)
                              }
                           ) {

                              Text(
                                 text = article.title,
                                 style = MaterialTheme.typography.bodyLarge
                              )

                              Divider(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))

                           }
                        }
                     }
                  }
               }
            }

            is UiState.Error -> {
               HandleUiStateError(
                  uiStateFlow = stateFlowHeadlines,
                  actionLabel = "Ok",
                  onErrorAction = { },
                  snackbarHostState = snackbarHostState,
                  navController = navController,
                  routePopBack = NavScreen.Headlines.route,
                  onUiStateFlowChange = { },
                  tag = tag
               )

            }
            else -> {}
         }
      }
   }
}
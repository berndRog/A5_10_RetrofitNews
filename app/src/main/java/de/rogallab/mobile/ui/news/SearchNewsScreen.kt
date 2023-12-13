package de.rogallab.mobile.ui.news

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.rogallab.android.ui.news.NewsItem
import de.rogallab.mobile.R
import de.rogallab.mobile.data.models.Article
import de.rogallab.mobile.data.models.News
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.composables.LogUiStates
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.news.composables.HandleUiStateError

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun SearchNewsScreen(
   viewModel: NewsViewModel,
   navController: NavController
) {
   //12345678901234567890123
   val tag = "ok>SearchNewsScreen   ."


   val uiStateEverythingFlow by viewModel.stateFlowEverything.collectAsState()
//         by viewModel.uiStateFlowEverything.collectAsStateWithLifecycle()
   LogUiStates(uiStateEverythingFlow, "UiState News", tag)


   val snackbarHostState = remember { SnackbarHostState() }

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
            title = { Text(stringResource(R.string.searchnews)) },
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
         val keyboardController = LocalSoftwareKeyboardController.current

         OutlinedTextField(
            modifier = Modifier
               .padding(horizontal = 8.dp).padding(bottom = 8.dp)
               .fillMaxWidth(),
            value = viewModel.searchText,
            onValueChange = { viewModel.onSearchTextChange(it) },
            label = {
               Text(text = stringResource(R.string.name))
            },
            leadingIcon = {
               Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search News")
            },
            keyboardOptions = KeyboardOptions(
               keyboardType = KeyboardType.Text,
               imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
               onSearch = {
                  viewModel.readEverything(viewModel.searchText)
                  keyboardController?.hide()
               }
            ),
            textStyle = MaterialTheme.typography.titleMedium,
            singleLine = true,
         )

         when (uiStateEverythingFlow) {
            UiState.Empty -> { logDebug(tag, "Empty")}

            UiState.Loading -> {
               logDebug(tag, "Loading...")
               Column(
                  modifier = Modifier
                     .padding(horizontal = 8.dp)
                     .fillMaxSize(),
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally
               ) {
                  CircularProgressIndicator(modifier = Modifier.size(160.dp))
               }
            }

            is UiState.Success -> {
               (uiStateEverythingFlow as UiState.Success).data?.let { news: News ->
                  logDebug(tag, "searchHeadlines Success ${news.totalResults}")
                  LazyColumn(
                     state = rememberLazyListState()
                  ) {
                     items(news.articles) { article: Article ->
                        NewsItem(
                           article,
                           onClick = {
                              viewModel.article = article
                              navController.navigate(NavScreen.Article.route)
                           }
                        )
                     }
                  }
               }
            }

            is UiState.Error -> {
               HandleUiStateError(
                  uiStateFlow = uiStateEverythingFlow,
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
package de.rogallab.mobile.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.rogallab.mobile.ui.news.HeadLinesScreen
import de.rogallab.mobile.ui.news.SearchNewsScreen
import de.rogallab.mobile.ui.news.ArticleScreen
import de.rogallab.mobile.ui.news.NewsViewModel
import de.rogallab.mobile.ui.news.SavedArticlesScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavHost(
   viewModel: NewsViewModel = hiltViewModel(),
) {
   val navHostController: NavHostController = rememberNavController()
   val duration = 800  // in ms

   NavHost(
      navController = navHostController,
      startDestination = NavScreen.Headlines.route,
      enterTransition = { enterTransition(duration) },
      exitTransition = { exitTransition(duration) },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {
      composable(route = NavScreen.Headlines.route) {
         HeadLinesScreen(
            viewModel = viewModel,
            navController = navHostController
         )
      }
      composable(route = NavScreen.SearchNews.route) {
         SearchNewsScreen(
            viewModel = viewModel,
            navController = navHostController
         )
      }
      composable(route = NavScreen.SavedArticles.route) {
         SavedArticlesScreen(
            viewModel = viewModel,
            navController = navHostController
         )
      }

      composable(
         route = NavScreen.Article.route,
      ) {
         viewModel.article?.let {
            ArticleScreen(
               navController = navHostController,
               viewModel = viewModel
            )
         }
      }
   }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
   duration: Int
) = fadeIn(animationSpec = tween(duration)) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Left
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
   duration: Int
) = fadeOut(animationSpec = tween(duration)) + slideOutOfContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Left
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(
   duration: Int
) = fadeIn(animationSpec = tween(duration)) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Up
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(
   duration: Int
) = fadeOut(animationSpec = tween(duration))
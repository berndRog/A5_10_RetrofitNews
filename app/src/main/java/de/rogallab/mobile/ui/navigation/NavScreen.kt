package de.rogallab.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.SavedSearch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.selects.select

sealed class NavScreen(
   val route: String,
   val title: String,
   val selectedIcon: ImageVector,
   val unSelectedIcon: ImageVector,
   val hasNews: Boolean = false,
   val badgeCount: Int? = null
) {
   data object Headlines:  NavScreen(
      route= "headlines",
      title = "Schlagzeilen",
      selectedIcon = Icons.Outlined.Article,
      unSelectedIcon = Icons.Filled.Article
   )

   data object SearchNews:  NavScreen(
      route = "everything",
      title = "Suche Artikel",
      selectedIcon = Icons.Outlined.Search,
      unSelectedIcon = Icons.Filled.Search,
   )

   data object Article:  NavScreen(
      route = "article",
      title ="Artikel anzeigen",
      selectedIcon = Icons.Outlined.Article,
      unSelectedIcon = Icons.Filled.Article,
   )

   data object SavedArticles:  NavScreen(
      route = "saved",
      title = "Gespeichert",
      selectedIcon = Icons.Outlined.SavedSearch,
      unSelectedIcon = Icons.Filled.SavedSearch
   )
}
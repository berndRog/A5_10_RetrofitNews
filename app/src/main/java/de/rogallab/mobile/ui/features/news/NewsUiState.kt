package de.rogallab.mobile.ui.features.news

import androidx.compose.runtime.Immutable
import de.rogallab.mobile.domain.entites.Article

@Immutable
data class NewsUiState(
   val searchText: String = "",
   val loading: Boolean = false,
   val articles: List<Article> = emptyList(),
)
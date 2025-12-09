package de.rogallab.mobile.ui.features.article

import androidx.compose.runtime.Immutable
import de.rogallab.mobile.domain.entites.Article

@Immutable
data class ArticlesUiState(
   val loading: Boolean = false,
   val articles: List<Article> = emptyList(),
)
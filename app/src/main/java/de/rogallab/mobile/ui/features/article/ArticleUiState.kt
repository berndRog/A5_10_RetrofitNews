package de.rogallab.mobile.ui.features.article

import androidx.compose.runtime.Immutable
import de.rogallab.mobile.domain.entites.Article

@Immutable
data class ArticleUiState(
   val isNews: Boolean? = true,
   val article: Article? =  null,
)
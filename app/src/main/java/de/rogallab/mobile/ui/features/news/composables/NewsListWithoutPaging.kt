package de.rogallab.mobile.ui.features.news.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.ImageLoader
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.ui.features.article.composables.ArticleItem

@Composable
fun NewsListWithOutPaging(
   articles: List<Article>,
   onClick: (Article) -> Unit,
   imageLoader: ImageLoader
) {
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
            onClick = { onClick(article) },
            imageLoader = imageLoader
         )
      }
   }
}

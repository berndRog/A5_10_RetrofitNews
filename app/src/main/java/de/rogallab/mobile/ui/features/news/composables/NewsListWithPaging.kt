package de.rogallab.mobile.ui.features.news.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.ui.features.article.composables.ArticleItem

@Composable
fun NewsListWithPaging(
   pagingItems: LazyPagingItems<Article>,
   onClick: (Article) -> Unit,
   imageLoader: ImageLoader
) {
   LazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = rememberLazyListState()
   ) {
      // articles paged
      items(
         count = pagingItems.itemCount,
         key = pagingItems.itemKey { it.id!! }
      ) { index ->
         val article = pagingItems[index]
         if (article != null) {
            ArticleItem(
               article = article,
               onClick = { onClick(article) },
               imageLoader = imageLoader
            )
         }
      }

      // Loader & Error footer
      pagingItems.apply {
         when {
            loadState.refresh is LoadState.Loading ||
            loadState.append is LoadState.Loading -> {
               item {
                  Column(
                     modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                     verticalArrangement = Arrangement.Center,
                     horizontalAlignment = Alignment.CenterHorizontally
                  ) { CircularProgressIndicator() }
               }
            }

            loadState.refresh is LoadState.Error -> {
               val e = loadState.refresh as LoadState.Error
               item {
                  Text(
                     "Error: ${e.error.localizedMessage}",
                     color = MaterialTheme.colorScheme.error
                  )
               }
            }
         }
      }
   }
}

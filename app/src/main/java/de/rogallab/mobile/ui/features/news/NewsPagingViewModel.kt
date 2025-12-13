package de.rogallab.mobile.ui.features.news

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import coil.ImageLoader
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.navigation.INavHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class NewsPagingViewModel(
   repository: INewsRepository,
   imageLoader: ImageLoader,
   navHandler: INavHandler,
): NewsBaseViewModel(repository, imageLoader, navHandler) {

   // PAGING PIPELINE (WITH PAGING 3)
   private val pageSize = 20

   val pagedArticlesFlow: Flow<PagingData<Article>> =
      _reloadTrigger
         .flatMapLatest {
            // Read the latest search text only WHEN a reload is triggered.
            val query = newsUiStateFlow.value.searchText.trim()
            if (query.isBlank()) {
               flowOf(PagingData.empty<Article>())
            } else {
               logDebug(TAG, "getEverythingPaged($query)")
               repository.getEverythingPaged(
                  searchText = query,
                  pageSize = pageSize
               )
            }
         }
         .cachedIn(viewModelScope)

   companion object {
      private const val TAG = "<-NewsPagingViewModel"
   }
}
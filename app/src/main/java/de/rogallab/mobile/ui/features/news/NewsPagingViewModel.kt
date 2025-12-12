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
import kotlinx.coroutines.flow.onStart

class NewsPagingViewModel(
   repository: INewsRepository,
   imageLoader: ImageLoader,
   navHandler: INavHandler,
): NewsBaseViewModel(repository, imageLoader, navHandler) {


   // -------------------------------------------------------------------------
   // PAGING PIPELINE (WITH PAGING 3)
   //
   // Only used when _usePaging == true (UI will collect pagedArticlesFlow).
   // Does NOT change NewsUiState.loading; loading is reflected via loadState in UI.
   // -------------------------------------------------------------------------
   private val pageSize = 20

   val pagedArticlesFlow: Flow<PagingData<Article>> =
      _reloadTrigger
         .onStart {
            logDebug(TAG, "PagedArticlesFlow onStart()")
            emit(Unit) } // initial load, analogous to legacy init + reload()
         .flatMapLatest {
            // Read the latest search text only WHEN a reload is triggered.
            val query = newsUiStateFlow.value.searchText.trim()
            if (query.isBlank()) {
               // Avoid HTTP 400 by not calling the API with an empty query.
               logDebug(TAG, "Paging: empty query -> return empty PagingData")
               flowOf(PagingData.empty<Article>())
            } else {
               logDebug(TAG, "call getEverythingPaged(searchText=$query) (paging mode)")
               repository.getEverythingPaged(
                  searchText = query,
                  pageSize = pageSize
               )
                  .onStart {
                     logDebug(TAG, "show loading = true")
                  }

            }
         }
         .cachedIn(viewModelScope)



   companion object {
      private const val TAG = "<-NewsViewModel"
   }
}
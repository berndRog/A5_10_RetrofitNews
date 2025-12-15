package de.rogallab.mobile.ui.features.news

import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.updateState
import de.rogallab.mobile.ui.navigation.INavHandler
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class NewsViewModel(
   repository: INewsRepository,
   imageLoader: ImageLoader,
   navHandler: INavHandler,
) : NewsBaseViewModel(repository, imageLoader, navHandler) {

   // Initialize PIPELINE
   init {
      // start pipeline that updates NewsUiState.news
      logDebug(TAG, "startPipeline()")
      startPipeline()
   }

   // -------------------------------------------------------------------------
   // PIPELINE - WITHOUT PAGING
   //
   // Flow behavior:
   // 1. When reloadTrigger emits, flatMapLatest starts a new Remote Fetch Flow
   // 2. Before first value => set loading=true (via onStart)
   // 3. For each Result<News>:
   //      - Success => update news + loading=false
   //      - Failure => call handleErrorEvent + loading=false
   //
   // State updates always go through updateState to maintain MVI style.
   // -------------------------------------------------------------------------
   // Complete reactive pipeline with flatMapLatest
   private fun startPipeline() {
      logDebug(TAG, "startPipeline without paging")

      viewModelScope.launch {
         // create a new flow for each reload event
         _reloadTrigger
            // Cancel prior request whenever a new reload event occurs
            .flatMapLatest { // nothing to map in trigger
               val query = newsUiStateFlow.value.searchText.trim()
               logDebug(TAG, "getEverything() $query")
               _repository.getEverything(  // returns Flow<Result<List<Article>>>
                  searchText = query,
               )
                  .onStart { // Emit loading state before each new fetch
                     logDebug(TAG, "show loading = true")
                     updateState(_newsUiStateFlow) { copy(loading = true) }
                  }
            }
            .collect { result: Result<List<Article>> ->
               // Transform Result<List<Article>> into NewsUiState
               result
                  .onSuccess { articles: List<Article> ->
                     logDebug(TAG, "loading = false, articles = ${articles.size}")
                     updateState(_newsUiStateFlow) {
                        copy(loading = false, articles = articles) }
                  }
                  .onFailure { t: Throwable ->
                     logDebug(TAG, "loading = false, error = ${t.message}")
                     handleErrorEvent(t)
                     updateState(_newsUiStateFlow) { copy(loading = false) }
                  }
            }
      }
   }

   companion object {
      private const val TAG = "<-NewsViewModel"
   }
}
package de.rogallab.mobile.ui.features.news

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import de.rogallab.mobile.data.mappings.toArticle
import de.rogallab.mobile.data.remote.dtos.NewsDto
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.base.updateState
import de.rogallab.mobile.ui.navigation.INavHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoilApi::class)
class NewsViewModel(
   private val _repository: INewsRepository,
   private val _imageLoader: ImageLoader,
   private val _navHandler: INavHandler,
   private val _usePaging: Boolean = false
) : BaseViewModel(_navHandler, TAG) {

   // Expose paging flag for UI (e.g. to switch layout)
   val usePaging: Boolean
      get() = _usePaging

   // NEWS UiStateFlow
   private val _newsUiStateFlow = MutableStateFlow(NewsUiState(loading = false))
   val newsUiStateFlow: StateFlow<NewsUiState> = _newsUiStateFlow.asStateFlow()

   // MVI pattern: transform intent into an action
   fun onProcessIntent(intent: NewsIntent) {
      logDebug(TAG, "onProcessIntent: $intent")
      when (intent) {
         is NewsIntent.SearchTextChange -> onSearchChange(intent.searchText)
         is NewsIntent.Reload -> reload()
      }
   }

   // SEARCHBAR
   private fun onSearchChange(searchText: String) {
      logDebug(TAG, "searchText: ($searchText) (${_newsUiStateFlow.value.searchText})")
      // Avoid unnecessary recompositions and reloads
      val trimmed = searchText.trim()
      if (trimmed.isBlank() || trimmed == _newsUiStateFlow.value.searchText) return
      // Update search UI state atomically
      updateState(_newsUiStateFlow) { copy(searchText = trimmed) }
   }

   // 1) EVENT FLOW – triggers reloads (pull-to-refresh, search, etc.)
   private val reloadTrigger = MutableSharedFlow<Unit>(
      replay = 1,                 // new collectors get the last reload event instantly
      extraBufferCapacity = 1     // prevents suspension when emitting rapidly
   )
   // Public API for UI to request a reload
   fun reload() {
      viewModelScope.launch {
         reloadTrigger.emit(Unit)
      }
   }

   // 2) Initialize REACTIVE PIPELINE or PAGING PIPELINE
   init {
      if (!_usePaging) {
         // Legacy mode: start old pipeline that updates NewsUiState.news
         logDebug(TAG, "NewsViewModel initialized, i.e. startPipeline()")
         startPipeline()
         reload()
      } else {
         // Paging mode: do not start legacy pipeline, only trigger initial reload
         logDebug(TAG, "NewsViewModel initialized in PAGING mode")
         reload()
      }
   }

   // -------------------------------------------------------------------------
   // REACTIVE PIPELINE - WITHOUT PAGING
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

      // create a new flow for each reload event
      reloadTrigger
         // Cancel prior request whenever a new reload event occurs
         .flatMapLatest { // nothing to map in trigger
            logDebug(TAG, "call Webservice getEverything() ")
            _repository.getEverything(
               searchText = _newsUiStateFlow.value.searchText,
            )
         }
         // Emit loading state before each new fetch
         .onStart {
            logDebug(TAG, "show loading = true")
            updateState(_newsUiStateFlow) { copy(loading = true) }
         }
         .map { result: Result<List<Article>> ->
            // Transform Result<List<Article>> into NewsUiState
            result.fold(
               onSuccess = { articles: List<Article> ->
                  logDebug(TAG, "loading = false, articles = ${articles.size}")
                  updateState(_newsUiStateFlow) {
                     copy(loading = false, articles = articles) }
               },
               onFailure = { throwable ->
                  logDebug(TAG, "loading = false, error = ${throwable.message}")
                  handleErrorEvent(throwable)
                  updateState(_newsUiStateFlow) { copy(loading = false) }
               }
            )
         }
         // Launch the entire pipeline inside the ViewModel scope
         .launchIn(viewModelScope)  // collect flow
   }

   // -------------------------------------------------------------------------
   // PAGING PIPELINE (WITH PAGING 3)
   //
   // Only used when _usePaging == true (UI will collect pagedArticlesFlow).
   // Does NOT change NewsUiState.loading; loading is reflected via loadState in UI.
   // -------------------------------------------------------------------------
   private val pageSize = 20

   // Expose PagingData<Article> for UI. When _usePaging == false, this flow
   // still exists, but you simply do not use it in the legacy screen.
   val pagedArticlesFlow: Flow<PagingData<Article>> =
      reloadTrigger
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
               _repository.getEverythingPaged(
                  searchText = query,
                  pageSize = pageSize
               )
            }
         }
         .cachedIn(viewModelScope)


   @OptIn(ExperimentalCoilApi::class)
   override fun onCleared() {
      logDebug(TAG, "onCleared(): clear caches")
      _imageLoader.memoryCache?.clear()
      _imageLoader.diskCache?.clear()
      super.onCleared()
   }

   companion object {
      private const val TAG = "<-NewsViewModel"
   }
}



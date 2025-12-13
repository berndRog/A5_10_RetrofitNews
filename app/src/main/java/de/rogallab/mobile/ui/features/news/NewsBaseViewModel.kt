package de.rogallab.mobile.ui.features.news

import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.base.updateState
import de.rogallab.mobile.ui.navigation.INavHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoilApi::class)
abstract class NewsBaseViewModel(
   protected val _repository: INewsRepository,
   protected val _imageLoader: ImageLoader,
   protected val _navHandler: INavHandler,
) : BaseViewModel(_navHandler, TAG) {

   // NEWS UiStateFlow
   protected val _newsUiStateFlow = MutableStateFlow(NewsUiState(loading = false))
   val newsUiStateFlow: StateFlow<NewsUiState> = _newsUiStateFlow.asStateFlow()

   // MVI pattern: transform intent into an action
   fun onProcessIntent(intent: NewsIntent) {
      logDebug(TAG, "onProcessIntent: $intent")
      when (intent) {
         is NewsIntent.SearchTextChange -> onSearchChange(intent.searchText)
         is NewsIntent.Reload -> reload()
         is NewsIntent.ShowError -> handleErrorEvent(intent.throwable)
      }
   }

   // SEARCHBAR
   protected open fun onSearchChange(searchText: String) {
      val query = searchText.trim()
      logDebug(TAG, "searchText: ($query) ")
      // Avoid unnecessary recompositions and reloads
      if (query.isBlank() || query == _newsUiStateFlow.value.searchText) return
      // Update search UI state atomically
      updateState(_newsUiStateFlow) { copy(searchText = query) }
   }

   // 1) EVENT FLOW – triggers reloads (pull-to-refresh, search, etc.)
   protected val _reloadTrigger = MutableSharedFlow<Unit>(
      replay = 0,
      extraBufferCapacity = 1     // prevents suspension when emitting rapidly
   )
   // Public API for UI to request a reload
   fun reload() {
      viewModelScope.launch {
         _reloadTrigger.emit(Unit)
      }
   }


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



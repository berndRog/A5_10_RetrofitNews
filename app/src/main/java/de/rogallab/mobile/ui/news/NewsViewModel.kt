package de.rogallab.mobile.ui.news

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.data.models.Article
import de.rogallab.mobile.data.models.News
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.INewsUsecases
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
   private val _context: Context,
   private val _useCases: INewsUsecases,
   private val _repository: INewsRepository,
   private val _dispatcher: CoroutineDispatcher
) : ViewModel() {

   private val tag = "ok>NewsViewModel      ."

   var _searchText: String by mutableStateOf(value = "")
   val searchText
      get() = _searchText
   fun onSearchTextChange(value: String) {
      if (value != _searchText) _searchText = value
   }

   var article: Article? = null

   // Coroutines ExceptionHandler, Context, Scope
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      exception.localizedMessage?.let {
         logError(tag, it)
         _stateFlowError.value = UiState.Error(it, true)
      } ?: run {
         exception.stackTrace.forEach {
            logError(tag, it.toString())
         }
      }
   }
   private val _coroutineContext = SupervisorJob() + _dispatcher + _exceptionHandler
   private val _coroutineScope = CoroutineScope(_coroutineContext)

   override fun onCleared() {
      // cancel all coroutines, when lifecycle of the viewmodel ends
      logDebug(tag, "Cancel all child coroutines")
      _coroutineContext.cancelChildren()
      _coroutineContext.cancel()
   }

   var _stateFlowError: MutableStateFlow<UiState<*>> = MutableStateFlow(UiState.Empty)
   val stateFlowError: StateFlow<UiState<*>>
      get() = _stateFlowError

   var country = "de"
   var headlinesPage = 1
   var everythingPage = 1

   var stateFlowHeadlines: StateFlow<UiState<News>> = MutableStateFlow(UiState.Empty)
      private set
   fun readHeadlines(country: String): Unit {
      logDebug(tag, "readHeadlines")
      stateFlowHeadlines = _useCases.fetchHeadlines(country, headlinesPage)
         .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            UiState.Empty
         )
   }
   init {
      readHeadlines(country)
   }

   private var _stateFlowEverything: MutableStateFlow<UiState<News>> =
      MutableStateFlow(UiState.Empty)
   val stateFlowEverything: StateFlow<UiState<News>>
      get() = _stateFlowEverything
   fun readEverything(query: String?): Unit {
      logDebug(tag, "readEverything $query")
      _coroutineScope.launch {
         _useCases.fetchEverything(query, everythingPage).collect { it: UiState<News> ->
            _stateFlowEverything.value = it
            delay(1000L)
         }
      }
   }

   var stateFlowArticles: StateFlow<UiState<List<Article>>> = flow {
      _useCases.readSavedArticles().collect { it: UiState<List<Article>> ->
         emit(it)
      }
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(1_000),
      UiState.Empty
   )
      private set

   fun upsert(article: Article) {
      try {
         article?.let {
            _coroutineScope.launch {
               val result = _coroutineScope.async {
                  _repository.upsert(it)
               }.await()
               if (!result) {
                  val message = "Error in upsert()"
                  logError(tag, message)
                  _stateFlowError.value = UiState.Error(message, false, true)
               }
            }
         }
      } catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag, message)
         _stateFlowError.value = UiState.Error(message, false, true)
      }
   }

   fun delete(article: Article) {
      try {
         article?.let {
            _coroutineScope.launch {
               val result = _coroutineScope.async {
                  _repository.delete(it)
               }.await()
               if (!result) {
                  val message = "Error in delete()"
                  logError(tag, message)
                  _stateFlowError.value = UiState.Error(message, false, true)
               }
            }
         }
      }
      catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag, message)
         _stateFlowError.value = UiState.Error(message, false, true)
      }
   }
}

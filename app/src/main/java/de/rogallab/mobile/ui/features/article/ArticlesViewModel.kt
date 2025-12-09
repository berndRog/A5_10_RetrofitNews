package de.rogallab.mobile.ui.features.article

import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.IArticleRepository
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.base.updateState
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.ArticleWeb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ArticlesViewModel(
   private val _repository: IArticleRepository,
   private val _navHandler: INavHandler,
) : BaseViewModel(_navHandler, TAG) {

   // A R T I C L E S   L I S T   S C R E E N
   private val _articlesUiStateFlow: MutableStateFlow<ArticlesUiState> =
      MutableStateFlow(ArticlesUiState())
   val articlesUiStateFlow: StateFlow<ArticlesUiState> =
      _articlesUiStateFlow.asStateFlow()

   // Start reactive Room pipeline
   init {
      fetch()
   }
   // Read all articles from the repository and keep ArticlesUiState in sync
   // with the reactive Room-backed flow.
   private fun fetch() {
      viewModelScope.launch {
         _repository.selectArticles()
            .onStart {
               updateState(_articlesUiStateFlow) { copy(loading = true) }
            }
            // Handle exceptions coming from the upstream flow
            .catch { t ->
               updateState(_articlesUiStateFlow) { copy(loading = false) }
               handleErrorEvent(t)
            }
            // Collect the Result<List<Article>> emitted by the Room-backed flow.
            // Room will emit again whenever the underlying table changes.
            .collectLatest { result: Result<List<Article>> ->
               result
                  .onSuccess { articles: List<Article> ->
                     logDebug(TAG,
                        "selectArticles() -> onSuccess: set loading = false, size = ${articles.size}")
                     val snapshot = articles.toList()
                     updateState(_articlesUiStateFlow) { copy(loading = false, articles = snapshot) }
                  }
                  .onFailure { t ->
                     updateState(_articlesUiStateFlow) { copy(loading = false) }
                     handleErrorEvent(t)
                  }
            }
      } // end launch
   }


   // A R T I C L E   W E B    S C R E E N
   private val _articleUiStateFlow: MutableStateFlow<ArticleUiState> =
      MutableStateFlow(ArticleUiState())
   val articleUiStateFlow: StateFlow<ArticleUiState> =
      _articleUiStateFlow.asStateFlow()

   // transform intent into an action
   fun onProcessIntent(intent: ArticleIntent) {
      when (intent) {
         is ArticleIntent.ShowWebArticle -> showWebArticle(intent.isNews, intent.article)
         is ArticleIntent.SaveArticle -> upsert()
         is ArticleIntent.RemoveArticle -> remove(intent.article)
      }
   }

   private fun showWebArticle(isNews: Boolean, article: Article) {
      updateState(_articleUiStateFlow) {
         copy(isNews = isNews, article = article)
      }
      _navHandler.push(ArticleWeb)
   }

   private fun upsert() {
      _articleUiStateFlow.value.article?.let { article ->
         viewModelScope.launch() {
            _repository.upsert(article).fold(
               onSuccess = { },
               onFailure = { t: Throwable -> handleErrorEvent(t) }
            )
         }
      }
   }

   private fun remove(article: Article) {
      viewModelScope.launch {
         _repository.remove(article).fold(
            onSuccess = {  },
            onFailure = { t: Throwable -> handleErrorEvent(t) }
         )
      }
   }

   companion object {
      private const val TAG = "<-ArticlesViewModel"
   }
}
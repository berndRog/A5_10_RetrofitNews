package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.data.models.Article
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReadSavedArticles @Inject constructor(
   private val _repository: INewsRepository,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) {

   operator fun invoke(): Flow<UiState<List<Article>>> = flow {
      emit(UiState.Loading)
      _repository.selectArticles().distinctUntilChanged().collect { articles: List<Article> ->
         delay(500)
         logDebug(tag, "invoke() emit success")
         emit(UiState.Success(data = articles))
      }
   }.catch {
      val message = it.localizedMessage ?: it.stackTraceToString()
      logError(tag, message)
      emit(UiState.Error(message = message))
   }.flowOn(_dispatcher + _exceptionHandler)

   companion object {
                                     //12345678901234567890123
      private const val tag: String = "ok>ReadSavedArticlesUC"
   }
}
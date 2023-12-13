package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.data.models.News
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FetchHeadlines @Inject constructor(
   private val _newsRepository: INewsRepository,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) {

   operator fun invoke(country: String, page: Int): Flow<UiState<News>> = flow {
      logDebug(tag,"invoke()")
      emit(UiState.Loading)
      safeApiRequest(tag, _dispatcher,_exceptionHandler) {
         _newsRepository.getHeadlines(country, page)
      }.collect{ it: UiState<News> ->
         emit(it)
      }
   }
   companion object {
      // 12345678901234567890123
      private const val tag: String = "ok>FetchHeadlinesUC   ."
   }

}


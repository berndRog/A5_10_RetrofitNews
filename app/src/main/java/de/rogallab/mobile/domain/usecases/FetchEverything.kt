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

class FetchEverything @Inject constructor(
   private val _newsRepository: INewsRepository,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) {

   operator fun invoke(query: String?, page: Int): Flow<UiState<News>> = flow {
      logDebug(tag,"invoke()")
      emit(UiState.Loading)
      query?.let {
         safeApiRequest(tag, _dispatcher, _exceptionHandler) {
            _newsRepository.getEverything(query, page)
         }.collect { it: UiState<News> ->
            emit(it)
         }
      } ?: run {
         emit(UiState.Empty)
      }
   }
   companion object {
                                    // 12345678901234567890123
      private const val tag: String = "ok>FetchEverythingUC  ."
   }

}


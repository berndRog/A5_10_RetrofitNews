package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.data.models.News
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

suspend fun <T> safeApiRequest(
   tag: String,
   dispatcher: CoroutineDispatcher,
   exceptionHandler: CoroutineExceptionHandler,
   apiCall: suspend () -> Response<T>,
): Flow<UiState<T>> = flow {

   try {
      val response: Response<T> = apiCall()
      logResponse(tag, response)

      if (response.isSuccessful) {
         val body = response.body()
         body?.let { it: T ->
            emit(UiState.Success(it))
         } ?: run {
            val message = "isSuccessful is true, but body() is null"
            logError(tag, message)
            emit(UiState.Error(message))
         }

      } else {
         val message = "${response.code()}: ${HttpStatusMessage(response.code())}"
         logError(tag, message)
         emit(UiState.Error(message))
      }

   } catch (e: Exception) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, message)
      emit(UiState.Error(message))
   }
}.flowOn(dispatcher+exceptionHandler)


private fun HttpStatusMessage(httpStatusCode:Int) : String =
   when(httpStatusCode) {
      200 -> "OK"
      201 -> "Created"
      202 -> "Accepted"
      204 -> "No Content"
      400 -> "Bad Request"
      401 -> "Unauthorized"
      404 -> "Not Found"
      403 -> "Forbidden"
      404 -> "Not Found"
      405 -> "Method Not Allowed"
      406 -> "Not Acceptable"
      407 -> "Proxy Authentication Required"
      408 -> "Request Timeout"
      409 -> "Conflict"
      415 -> "Unsupported Media Type"
      500 -> "Internal Server Error"
      501 -> "Not Implemented"
      502 -> "Bad Gateway"
      503 -> "Service Unavailable"
      504 -> "Gateway Timeout"
      else -> "Unknown"
   }

private fun <T> logResponse(
   tag: String,
   response: Response<T>
) {
   logVerbose(tag, "Request ${response.raw().request.method} ${response.raw().request.url}")
   logVerbose(tag, "Request Headers")
   response.raw().request.headers.forEach {
      val text = "   %-15s %s".format(it.first, it.second )
      logVerbose(tag, "$text")
   }

   val ms = response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis
   logVerbose(tag, "took $ms ms")
   logVerbose(tag, "Response isSuccessful ${response.isSuccessful()}")

   logVerbose(tag, "Response Headers")
   response.raw().headers.forEach {
      val text = "   %-15s %s".format(it.first, it.second)
      logVerbose(tag, "$text")
   }

   logVerbose(tag, "Response Body")
   if (response.body() is News) {
      val news = response.body() as News
      val text = response.body().toString().substring(0,200)
      logVerbose(tag, "Response Body   $text")
      logVerbose(tag, "   NewsApi.articles.size ${news.articles.size}")
      logVerbose(tag, "   NewsApi.status        $news.status}")
      logVerbose(tag, "   NewsApi.totalResults  $news.totalResults}")
   }
   logVerbose(tag, "   Response Status Code ${response.code()}")
   logVerbose(tag, "   Response Status Message ${response.message()}")
}
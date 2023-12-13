package de.rogallab.mobile.ui.composables

import android.util.Log
import androidx.compose.runtime.Composable
import de.rogallab.mobile.domain.UiState

@Composable
fun <T> LogUiStates(
   uiStateFlow: UiState<T>?,
   text: String,
   tag: String,
) {
   uiStateFlow?.let { it ->
      val up = it.upHandler
      val back = it.backHandler
      when (it) {
         UiState.Empty      -> Log.v(tag, "Compos. $text.Empty $up $back")
         UiState.Loading    -> Log.v(tag, "Compos. $text.Loading $up $back")
         is UiState.Success -> Log.v(tag, "Compos. $text.Success $up $back")
         is UiState.Error   -> Log.v(tag, "Compos. $text.Error $up $back")
      }
   }
}
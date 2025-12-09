package de.rogallab.mobile.ui.features.article.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.Globals
import de.rogallab.mobile.domain.entites.Article
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeArticleListItem(
   article: Article,
   onNavigate: (String) -> Unit,
   onRemove: () -> Unit,
   content: @Composable () -> Unit
) {
   val tag = "<-SwipePersonListItem"

   // Ephemeral UI state controlling only the exit animation.
   // Reset automatically when person.id changes (fresh composition).
   var isRemoved by remember(article.id) { mutableStateOf(false) }

   val state = rememberSwipeToDismissBoxState(
      positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
   )

   // React to swipe gestures emitted by SwipeToDismissBox.
   // We only use gestures as triggers; we never keep the internal dismissed state.
   LaunchedEffect(state.currentValue) {
      when (state.currentValue) {

         // LEFT → RIGHT: navigate to edit screen
         SwipeToDismissBoxValue.StartToEnd -> {
            logDebug(tag, "Swipe to Edit for ${article.title}")
            onNavigate(article.id)
            // Immediately snap back; we manage visuals ourselves
            state.snapTo(SwipeToDismissBoxValue.Settled)
         }

         // RIGHT → LEFT: start delete animation
         SwipeToDismissBoxValue.EndToStart -> {
            logDebug(tag, "Swipe to Delete for ${article.title} ")
            isRemoved = true   // triggers AnimatedVisibility exit
            state.snapTo(SwipeToDismissBoxValue.Settled)
         }

         SwipeToDismissBoxValue.Settled -> Unit
      }
   }

   // Ensure the swipe state resets cleanly when a new person instance appears.
   LaunchedEffect(article.id) {
      state.snapTo(SwipeToDismissBoxValue.Settled)
   }

   // After the exit animation completes, perform the actual deletion
   // via ViewModel → Repository → DataStore/Room.
   LaunchedEffect(isRemoved, article.id) {
      if (isRemoved) {
         delay(Globals.animationDuration.toLong())
         onRemove()     // no Undo → remove immediately
      }
   }

   // Shrink + fade-out when item is dismissed.
   AnimatedVisibility(
      visible = !isRemoved,
      exit = shrinkVertically(
         animationSpec = tween(durationMillis = Globals.animationDuration),
         shrinkTowards = Alignment.Top
      ) + fadeOut(
         animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh,
            visibilityThreshold = 0.002f
         )
      )
   ) {
      SwipeToDismissBox(
         state = state,
         backgroundContent = { SwipeSetBackground(state) },
         enableDismissFromStartToEnd = true,
         enableDismissFromEndToStart = true,
         modifier = Modifier.padding(vertical = 4.dp)
      ) {
         content()
      }
   }
}

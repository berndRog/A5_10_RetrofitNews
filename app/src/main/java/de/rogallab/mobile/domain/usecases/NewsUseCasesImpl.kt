package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.domain.INewsUsecases
import javax.inject.Inject

data class NewsUseCasesImpl @Inject constructor(
   override val fetchHeadlines: FetchHeadlines,
   override val fetchEverything: FetchEverything,
   override val readSavedArticles: ReadSavedArticles
) : INewsUsecases



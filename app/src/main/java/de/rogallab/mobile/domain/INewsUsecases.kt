package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.usecases.FetchEverything
import de.rogallab.mobile.domain.usecases.FetchHeadlines
import de.rogallab.mobile.domain.usecases.ReadSavedArticles

interface INewsUsecases {
   val fetchHeadlines: FetchHeadlines
   val fetchEverything: FetchEverything
   val readSavedArticles: ReadSavedArticles
}
package de.rogallab.mobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import de.rogallab.mobile.data.repositories.NewsRepositoryImpl
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.INewsUsecases
import de.rogallab.mobile.domain.usecases.NewsUseCasesImpl

@InstallIn(ViewModelComponent::class)
@Module
abstract class BindViewModelModules {
   @ViewModelScoped
   @Binds
   abstract fun bindPeopleUseCases(
      peopleUseCases: NewsUseCasesImpl
   ): INewsUsecases
   @Binds
   @ViewModelScoped
   abstract fun bindNewsRepository(
      newsRepositoryImpl: NewsRepositoryImpl
   ): INewsRepository
}
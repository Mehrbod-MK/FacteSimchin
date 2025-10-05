package com.mehrbodmk.factesimchin.di

import android.app.Application
import com.mehrbodmk.factesimchin.db.MafiaDatabase
import com.mehrbodmk.factesimchin.player.useCases.UseCasePlayer
import com.mehrbodmk.factesimchin.player.useCases.UseCasePlayerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun database(application: Application) : MafiaDatabase {
        return MafiaDatabase.getInstance(application)
    }

    @Provides
    fun provideUseCasePlayer(db: MafiaDatabase) : UseCasePlayer {
        return UseCasePlayerImpl(db.daoBase())
    }

}
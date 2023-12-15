package com.example.exploringexoplayer.data.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.exploringexoplayer.data.preferences.AUDIO_EFFECT_PREFERENCES
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder.create()
    }

    @Named(AUDIO_EFFECT_PREFERENCES)
    @Provides
    fun provideAudioEffectPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(AUDIO_EFFECT_PREFERENCES, Context.MODE_PRIVATE)
    }
}
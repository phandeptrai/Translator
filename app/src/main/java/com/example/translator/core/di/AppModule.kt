package com.example.translator.core.di

import android.content.Context
import androidx.room.Room
import com.example.translator.data.datasource.local.TranslationDao
import com.example.translator.data.datasource.local.TranslationDatabase
import com.example.translator.data.datasource.remote.TranslationApi
import com.example.translator.data.repository.TranslationRepositoryImpl
import com.example.translator.domain.repository.TranslationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindTranslationRepository(
        translationRepositoryImpl: TranslationRepositoryImpl
    ): TranslationRepository

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        }

        @Provides
        @Singleton
        fun provideTranslationApi(okHttpClient: OkHttpClient): TranslationApi {
            return Retrofit.Builder()
                .baseUrl("https://api.example.com/") // Thay thế bằng URL thực tế của API
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TranslationApi::class.java)
        }

        @Provides
        @Singleton
        fun provideTranslationDatabase(
            @ApplicationContext context: Context
        ): TranslationDatabase {
            return Room.databaseBuilder(
                context,
                TranslationDatabase::class.java,
                "translations.db"
            ).build()
        }

        @Provides
        @Singleton
        fun provideTranslationDao(database: TranslationDatabase): TranslationDao {
            return database.translationDao
        }
    }
} 
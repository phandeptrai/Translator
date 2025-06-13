package com.example.translator.di

import com.example.translator.repository.TranslateRepository
import com.example.translator.service.TranslateApiService
import com.example.translator.viewmodel.TranslateViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    // Định nghĩa Retrofit
    single { provideRetrofit() }
    // Định nghĩa TranslateApiService
    single { provideTranslateApiService(get()) }
    // Định nghĩa TranslateRepository
    single { TranslateRepository(get()) }
    // Định nghĩa TranslateViewModel
    viewModel { TranslateViewModel(get()) }
}

private fun provideRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://api.mymemory.translated.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private fun provideTranslateApiService(retrofit: Retrofit): TranslateApiService {
    return retrofit.create(TranslateApiService::class.java)
}
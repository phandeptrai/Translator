package com.example.translator.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Translations::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao
}
package com.example.translator.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.translator.domain.model.Translation

@Database(
    entities = [Translation::class],
    version = 1,
    exportSchema = false
)
abstract class TranslationDatabase : RoomDatabase() {
    abstract val translationDao: TranslationDao
} 
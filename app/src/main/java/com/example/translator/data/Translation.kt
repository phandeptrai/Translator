package com.example.translator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class Translation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inputText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val timestamp: Long
)
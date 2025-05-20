package com.example.translator.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.room.Room
import com.example.translator.data.AppDatabase

class TranslationProvider : ContentProvider() {
    private lateinit var db: AppDatabase

    companion object {
        val AUTHORITY = "com.example.translator.provider"
        val CONTENT_URI = Uri.parse("content://$AUTHORITY/translations")
    }

    override fun onCreate(): Boolean {
        db = Room.databaseBuilder(
            context!!,
            AppDatabase::class.java,
            "translator-db"
        ).build()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor = db.translationDao().getAllTranslations()
        return cursor as? Cursor
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
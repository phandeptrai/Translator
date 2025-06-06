package com.example.translator

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

// Entity cho lịch sử dịch
@Entity(tableName = "translation_history")
data class TranslationHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val timestamp: Date = Date()
)

// DAO cho lịch sử dịch
@Dao
interface TranslationHistoryDao {
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TranslationHistoryItem>>

    @Insert
    suspend fun insert(item: TranslationHistoryItem): Long

    @Delete
    suspend fun delete(item: TranslationHistoryItem)

    @Query("DELETE FROM translation_history")
    suspend fun deleteAll()
}

// Converters cho Room
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

// Database
@Database(entities = [TranslationHistoryItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TranslationHistoryDatabase : RoomDatabase() {
    abstract fun translationHistoryDao(): TranslationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: TranslationHistoryDatabase? = null

        fun getDatabase(context: Context): TranslationHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TranslationHistoryDatabase::class.java,
                    "translation_history_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Manager cho lịch sử dịch
class TranslationHistoryManager(context: Context) {
    private val database = TranslationHistoryDatabase.getDatabase(context)
    private val dao = database.translationHistoryDao()

    // Lấy tất cả lịch sử dịch
    fun getAllHistory(): Flow<List<TranslationHistoryItem>> {
        return dao.getAllHistory()
    }

    // Thêm một mục vào lịch sử
    suspend fun addToHistory(
        sourceText: String,
        translatedText: String,
        sourceLanguage: String,
        targetLanguage: String
    ) = withContext(Dispatchers.IO) {
        val historyItem = TranslationHistoryItem(
            sourceText = sourceText,
            translatedText = translatedText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage
        )
        dao.insert(historyItem)
    }

    // Xóa một mục khỏi lịch sử
    suspend fun deleteFromHistory(item: TranslationHistoryItem) = withContext(Dispatchers.IO) {
        dao.delete(item)
    }

    // Xóa tất cả lịch sử
    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}

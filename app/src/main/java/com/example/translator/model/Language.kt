package com.example.translator.model

enum class Language(
    val code: String,
    val displayName: String
) {
    ENGLISH(code = "en", displayName = "Tiếng Anh"),
    VIETNAMESE(code = "vi", displayName = "Tiếng Việt"),
    FRENCH(code = "fr", displayName = "Tiếng Pháp"),
    SPANISH(code = "es", displayName = "Tiếng Tây Ban Nha"),
    GERMAN(code = "de", displayName = "Tiếng Đức"),
    JAPANESE(code = "ja", displayName = "Tiếng Nhật"),
    KOREAN(code = "ko", displayName = "Tiếng Hàn"),
    CHINESE(code = "zh", displayName = "Tiếng Trung");

    companion object {
        fun fromCode(code: String): Language {
            return values().firstOrNull { it.code == code }
                ?: throw IllegalArgumentException("Ngôn ngữ không hỗ trợ: $code")
        }
    }
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.translator"
    compileSdk = 36 // Nâng cấp lên SDK 36 theo khuyến nghị

    defaultConfig {
        applicationId = "com.example.translator"
        minSdk = 31
        targetSdk = 34 // Giữ nguyên targetSdk vì nó không ảnh hưởng đến vấn đề biên dịch
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    // Giữ nguyên cấu hình này để xử lý tệp trùng lặp
    packagingOptions {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
            pickFirsts += listOf(
                "lib/arm64-v8a/libtranslate_jni.so",
                "lib/armeabi-v7a/libtranslate_jni.so",
                "lib/x86/libtranslate_jni.so",
                "lib/x86_64/libtranslate_jni.so"
            )
        }
    }
}

// Thêm chiến lược giải quyết xung đột phụ thuộc
configurations.all {
    resolutionStrategy {
        force("com.google.android.gms:play-services-basement:18.2.0")
        force("com.google.android.gms:play-services-base:18.2.0")
        force("com.google.android.gms:play-services-tasks:18.0.2")
    }
}

dependencies {
    // AndroidX Core - Sử dụng phiên bản tương thích với compileSdk 36
    implementation(libs.androidx.core.ktx) // Đảm bảo version catalog đã được cập nhật
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose) // Đảm bảo version catalog đã được cập nhật
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Retrofit & Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ML Kit
    implementation("com.google.mlkit:translate:16.1.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
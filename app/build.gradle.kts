plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.translator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.translator"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Nâng cấp để tương thích Kotlin 1.9.20
    }

    packaging {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1") // Nâng cấp
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6") // Nâng cấp
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6") // Thêm cho viewModelScope
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1") // Thêm cho coroutines

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // Nâng cấp
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // Nâng cấp
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Nâng cấp

    // Koin
    implementation("io.insert-koin:koin-core:3.5.0") // Nâng cấp
    implementation("io.insert-koin:koin-android:3.5.0") // Nâng cấp
    implementation("io.insert-koin:koin-androidx-compose:3.5.0") // Nâng cấp

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1") // Nâng cấp
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") // Nâng cấp
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00")) // Nâng cấp
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Compose
    implementation("androidx.activity:activity-compose:1.9.2") // Nâng cấp
    implementation(platform("androidx.compose:compose-bom:2024.06.00")) // Nâng cấp
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6") // Nâng cấp
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.material:material-icons-extended:1.6.0") // Thêm dependency cho icons mở rộng
}
// Thêm rules Proguard để bảo vệ Koin, Retrofit, và Compose
android {
    buildTypes {
        release {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Thêm rules tùy chỉnh nếu cần
        }
    }
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // ⭐ BẮT BUỘC: Cần kapt để xử lý các Annotation của Room
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.projectqlbenhan"

    // ⭐ FIX LỖI SDK: Nâng lên 36 để tương thích với các thư viện AndroidX mới nhất
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projectqlbenhan"
        minSdk = 24

        // ⭐ Giữ targetSdk ở 35 để đảm bảo hành vi ứng dụng ổn định trên máy người dùng
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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

    // ⭐ CẬP NHẬT: Java 17 là yêu cầu bắt buộc để chạy Daemon ổn định
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ⭐ CẬP NHẬT: Dùng bản Room 2.6.1 Stable để tránh lỗi biên dịch
    val room_version = "2.6.1"

    // Room components
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-paging:$room_version")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // UI & Core - Sử dụng các bản ổn định khớp với SDK
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Visualization
    implementation(libs.mpandroidchart)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
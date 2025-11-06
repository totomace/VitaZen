plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.vitazen"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.vitazen"
        minSdk = 30
        targetSdk = 36
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Dọn dẹp các khai báo trùng lặp
    implementation("androidx.navigation:navigation-compose:2.8.0-beta05")

    // --- PHẦN FIREBASE ĐÃ SỬA LỖI ---

    // 1. Import the Firebase BoM (Bill of Materials)
    // Dòng này phải được đặt TRƯỚC các thư viện Firebase khác.
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    // 2. Thêm các thư viện Firebase bạn cần.
    // Sử dụng BOM để quản lý version, nhưng firebase-auth-ktx cần explicit version
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")

    // ----------------------------------

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Dependencies cho Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

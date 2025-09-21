plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.picchallenge"
    compileSdk = 36
    buildToolsVersion = "36.1"

    defaultConfig {
        applicationId = "com.example.picchallenge"
        minSdk = 24
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
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Additional Compose dependencies
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Network - Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")
    
    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Paging 3
    implementation("androidx.paging:paging-compose:3.2.1")
    
    // Accompanist (permission handling)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    
    // Coil for image loading (alternative to Glide)
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Additional dependencies for the final app
    implementation("androidx.compose.material:material-icons-core:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

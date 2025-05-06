import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    kotlin("android") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
}

// Load properties from local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

// Get Supabase credentials with default fallback values
val supabaseUrl = localProperties.getProperty("supabase.url", "\"https://default.supabase.co\"")
val supabaseAnonKey = localProperties.getProperty("supabase.anon.key", "\"default-anon-key\"")

android {
    namespace = "com.example.expense_tracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.expense_tracker"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Adding Supabase credentials to BuildConfig in release build
            buildConfigField("String", "SUPABASE_URL", supabaseUrl)
            buildConfigField("String", "SUPABASE_ANON_KEY", supabaseAnonKey)
        }

        debug {
            buildConfigField("String", "BASE_URL", "\"http://localhost:8080/\"")
            
            // Adding Supabase credentials to BuildConfig in debug build
            buildConfigField("String", "SUPABASE_URL", supabaseUrl)
            buildConfigField("String", "SUPABASE_ANON_KEY", supabaseAnonKey)
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    

}


dependencies {
    implementation(libs.appcompat)
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.datastore.core.android)
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    
    // Supabase dependencies
    implementation("io.github.jan-tennert.supabase:storage-kt:1.1.0")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.1.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:1.1.0")
    // Force Kotlin stdlib version compatibility
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    implementation("io.ktor:ktor-client-android:2.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.room:room-ktx:2.7.0")
    // Preference for settings
    implementation("androidx.preference:preference:1.2.1")

    implementation(libs.swiperefreshlayout)
    
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.tasks)
    implementation(libs.firebase.auth)
    implementation(libs.litert.support.api)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.room.rxjava2)
    
    // Camera and permissions dependencies for receipt image feature
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Permission handling
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("com.guolindev.permissionx:permissionx:1.7.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
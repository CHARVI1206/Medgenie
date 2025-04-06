plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // If using Firebase/Google services
}

android {
    namespace = "com.example.medgenie"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        applicationId = "com.example.medgenie"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        // ✅ Inject API Key into BuildConfig
        val geminiApiKey = project.findProperty("GEMINI_API_KEY") as String? ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // ✅ Fixed packagingOptions deprecation
    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
        resources.excludes.add("META-INF/INDEX.LIST")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // Firebase and Google APIs
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation ("com.google.code.gson:gson:2.10")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.mlkit:translate:17.0.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
// Gemini SDK


    implementation("androidx.appcompat:appcompat:1.6.1")



    // Google AI Platform (Gemini) - Fixed Protobuf Issue
    implementation("com.google.cloud:google-cloud-aiplatform:3.40.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
    }

    // ML Kit - Fixed Protobuf Issue
    implementation("com.google.mlkit:text-recognition:16.0.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }

    // Google Play Services
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-identity:18.0.1")

    // ✅ Fixed Protobuf versioning
    implementation("com.google.protobuf:protobuf-javalite:3.25.2")

    // Material Components for Material 3
    implementation("com.google.android.material:material:1.11.0")

}

// ✅ Force correct dependency versions to prevent conflicts
configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:3.25.2")
        force("com.google.android.gms:play-services-auth:20.7.0")
        force("com.google.firebase:firebase-auth-ktx:22.3.1")
    }
}

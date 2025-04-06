// Top-level build.gradle file

plugins {
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Ensure the latest Google services Gradle plugin
        classpath ("com.android.tools.build:gradle:8.3.0")
        classpath ("com.google.gms:google-services:4.4.2")
    }
}



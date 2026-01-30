plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.nabthespy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nabthespy"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    buildToolsVersion = "35.0.0"
}

dependencies {

    /* ---------------- CORE ANDROID ---------------- */
    implementation(libs.androidx.core.ktx)      // must be 1.13.1 in version catalog
    implementation(libs.androidx.appcompat)
    implementation("com.google.android.material:material:1.13.0")

    /* ---------------- LIFECYCLE ---------------- */
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")

    /* ---------------- SECURITY ---------------- */
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation(libs.junit.junit)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.junit)

    /* ---------------- CAMERAX (STABLE) ---------------- */
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    /* ---------------- ML KIT ---------------- */
    implementation("com.google.mlkit:face-detection:16.1.7")

    /* ---------------- TENSORFLOW LITE ---------------- */
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")

    /* ---------------- IMAGE LOADING ---------------- */
    implementation("com.github.bumptech.glide:glide:4.16.0")

    /* ---------------- NAVIGATION ---------------- */
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")

    implementation("com.google.code.gson:gson:2.10.1")

    /* ---------------- TESTING ---------------- */
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")


}

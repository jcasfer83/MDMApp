plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.joaquin.mdmapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.joaquin.mdmapp"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("releaseConfig") {
            storeFile = file(project.findProperty("KEYSTORE_PATH") as String)
            storePassword = project.findProperty("KEYSTORE_PASSWORD") as String
            keyAlias = project.findProperty("KEY_ALIAS") as String
            keyPassword = project.findProperty("KEY_PASSWORD") as String
        }
    }
    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("releaseConfig")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
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
        dataBinding = true
        viewBinding = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    //Local unit tests
    testImplementation(libs.androidx.core)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.mockk)
    testImplementation(libs.ui.test.manifest)
    testImplementation(libs.mockito.core)
    testImplementation (libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(kotlin("test"))

    // Instrumetation tests
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.junit2)
    androidTestImplementation(libs.kotlinx.coroutines.test.v164)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.goggle.truth)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.mockwebserver.v4100)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core)

}
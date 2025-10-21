import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.mehrbodmk.factesimchin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mehrbodmk.factesimchin"
        minSdk = 28
        targetSdk = 36
        versionCode = 11
        versionName = "0.5.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val props = project.rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
        Properties().apply { load(it) }
    }

    fun getSecret(key: String): String? =
        System.getenv(key) ?: props?.getProperty(key)

    signingConfigs {
        create("release") {
            val keystorePath = getSecret("KEYSTORE_PATH")
            val keystorePassword = getSecret("KEYSTORE_PASSWORD")
            val keyAlias = getSecret("KEY_ALIAS")
            val keyPassword = getSecret("KEY_PASSWORD")

            if (keystorePath != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
                storeFile = File(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            } else {
                println("⚠️ Signing config skipped: missing credentials.")
            }
        }
    }


    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.gson)
}
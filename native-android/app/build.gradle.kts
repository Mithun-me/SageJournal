plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.aura.sagejournal"
    compileSdk = 36

    defaultConfig {
        // Suffixed so this installs alongside the shipping Capacitor build
        // during the port. Drops to com.aura.sagejournal when it replaces it.
        applicationId = "com.aura.sagejournal.lab"
        // AGSL (RuntimeShader) is API 33+. Production would need a fallback
        // for 24..32; the gate deliberately tests the intended path only.
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "0.1-gate"
    }

    buildTypes {
        // Measured in release: debug Compose carries composition tracing
        // overhead that would make any frame-time reading meaningless.
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { jvmTarget = "21" }
    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
}

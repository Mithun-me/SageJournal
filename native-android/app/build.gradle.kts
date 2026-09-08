plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.aura.sagejournal"
    compileSdk = 36

    defaultConfig {
        // Suffixed so this installs alongside the shipping Capacitor build
        // during the port. Drops to com.aura.sagejournal when it replaces it.
        applicationId = "com.aura.sagejournal.lab"
        // Matches the Capacitor build this replaces. The liquid background
        // tiers by API level — see LiquidBackground.
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1-gate"

    }

    buildTypes {
        debug {
            // Development points at the host over `adb reverse tcp:3000 tcp:3000`.
            buildConfigField("String", "AURA_API_BASE", "\"http://localhost:3000\"")
        }

        // Measured in release: debug Compose carries composition tracing
        // overhead that would make any frame-time reading meaningless.
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")

            // Supplied per build, never committed:
            //   ./gradlew assembleRelease -PauraApiBase=https://aura.example.com
            // or AURA_API_BASE in the environment.
            val configured = (project.findProperty("auraApiBase") as String?)
                ?: System.getenv("AURA_API_BASE")

            // A release that quietly points at localhost is a release whose AI
            // features are dead on every device but this one, with no error to
            // notice. Better to refuse to build.
            if (configured != null) {
                require(configured.startsWith("https://")) {
                    "auraApiBase must be https:// for a release build (got: $configured)"
                }
            }
            buildConfigField(
                "String", "AURA_API_BASE",
                "\"" + (configured ?: "") + "\"",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { jvmTarget = "21" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
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
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // Entries need search and per-day aggregation, so a real table
    // rather than a serialised blob.
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Settings are scalars; DataStore is the right size for them.
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Talks to the existing Express backend in server.ts.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

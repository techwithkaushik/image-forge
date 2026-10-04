plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingStorePath = System.getenv("SIGN_KEY_STORE")
val signingKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
val signingKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")
val signingStorePassword = System.getenv("SIGNING_STORE_PASSWORD")

android {
    namespace = "org.techwithkaushik.imageforge"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.techwithkaushik.imageforge"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    signingConfigs {
        create("release") {
            require(!signingStorePath.isNullOrBlank()) {
                "SIGN_KEY_STORE must be set for release builds."
            }
            require(!signingKeyAlias.isNullOrBlank()) {
                "SIGNING_KEY_ALIAS must be set for release builds."
            }
            require(!signingKeyPassword.isNullOrBlank()) {
                "SIGNING_KEY_PASSWORD must be set for release builds."
            }
            require(!signingStorePassword.isNullOrBlank()) {
                "SIGNING_STORE_PASSWORD must be set for release builds."
            }

            storeFile = file(signingStorePath!!)
            storePassword = signingStorePassword
            keyAlias = signingKeyAlias
            keyPassword = signingKeyPassword
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:dashboard"))

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}

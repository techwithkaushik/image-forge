plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.techwithkaushik.imageforge.designsystem"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
}

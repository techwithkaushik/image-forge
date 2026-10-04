plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.techwithkaushik.imageforge.feature.editor"
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
    implementation(project(":core:designsystem"))
    implementation(project(":core:imageprocessor"))
    implementation(project(":core:media"))
    implementation(project(":core:storage"))
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
}

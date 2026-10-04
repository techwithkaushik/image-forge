plugins {
    id("com.android.library")
}

android {
    namespace = "org.techwithkaushik.imageforge.media"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation("junit:junit:4.13.2")
}

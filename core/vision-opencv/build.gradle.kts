plugins {
    id("com.android.library")
}

android {
    namespace = "org.techwithkaushik.imageforge.vision"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation("org.opencv:opencv:5.0.0.1")
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation("junit:junit:4.13.2")
}

plugins {
    id("com.android.library")
}

android {
    namespace = "org.techwithkaushik.imageforge.imageprocessor"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.exifinterface)
    testImplementation("junit:junit:4.13.2")
}

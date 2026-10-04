plugins {
    id("com.android.library")
}

android {
    namespace = "org.techwithkaushik.imageforge.imageprocessor"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:common"))
}

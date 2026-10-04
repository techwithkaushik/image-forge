plugins {
    id("com.android.library")
}

android {
    namespace = "org.techwithkaushik.imageforge.ocr"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:common"))
}

plugins {
    id("com.android.library")
}

android {
    namespace = "org.techwithkaushik.imageforge.storage"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:common"))
}

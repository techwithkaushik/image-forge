plugins {
    id("com.android.library")
}

android {
    namespace = "org.techwithkaushik.imageforge.media"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:common"))
}

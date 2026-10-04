plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.library")
}

android {
    namespace = "org.techwithkaushik.imageforge.common"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
}


dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

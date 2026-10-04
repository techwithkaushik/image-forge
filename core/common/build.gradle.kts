plugins {
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
    testImplementation(kotlin("test"))
}

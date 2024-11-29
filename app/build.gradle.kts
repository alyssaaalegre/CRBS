plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.CRBS"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.CRBS"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // New dependencies (reference from libs)
    implementation(libs.google.material)
    implementation(libs.androidx.viewpager2)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

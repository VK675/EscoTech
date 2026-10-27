plugins {
    alias(libs.plugins.android.application)
    // Lê o ficheiro google-services.json (descarregado da consola do Firebase)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.escotech"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.escotech"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        // Gera uma classe "Binding" para cada layout XML (ex.: ActivityLoginBinding)
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)

    // Firebase: a BoM escolhe versões compatíveis entre as bibliotecas
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

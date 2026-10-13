plugins {
    alias(aclibs.plugins.android.application)
    alias(aclibs.plugins.kotlin.compose)
    alias(aclibs.plugins.ksp)
}

android {
    namespace = "com.example.aquacontrol"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.aquacontrol"
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
        compose = true
    }
}

dependencies {
    implementation(platform(aclibs.androidx.compose.bom))
    implementation(aclibs.androidx.compose.ui)
    implementation(aclibs.androidx.compose.ui.graphics)
    implementation(aclibs.androidx.compose.ui.tooling.preview)
    implementation(aclibs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    implementation(aclibs.androidx.activity.compose)
    implementation(aclibs.androidx.core.ktx)
    implementation(aclibs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Carga asíncrona y caché de imágenes locales.
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")

    // Room: persistencia local y generación de código mediante KSP
    implementation(aclibs.room.runtime)
    implementation(aclibs.room.ktx)
    ksp(aclibs.room.compiler)

    // Pruebas
    testImplementation(aclibs.junit)
    androidTestImplementation(platform(aclibs.androidx.compose.bom))
    androidTestImplementation(aclibs.androidx.compose.ui.test.junit4)
    androidTestImplementation(aclibs.androidx.espresso.core)
    androidTestImplementation(aclibs.androidx.junit)

    // Herramientas de Compose
    debugImplementation(aclibs.androidx.compose.ui.tooling)
    debugImplementation(aclibs.androidx.compose.ui.test.manifest)

    // Comunicación con API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
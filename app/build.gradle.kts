plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.weatherapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.weatherapp"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // 1. Thư viện Retrofit & GSON (Dùng để gọi API thời tiết)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 2. Thư viện Glide (Dùng để tải icon thời tiết từ link web)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // 3. Thư viện dịch vụ định vị của Google Play Services
    implementation("com.google.android.gms:play-services-location:21.2.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Google firebase
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))

    // Thư viện Đăng nhập Firebase
    implementation("com.google.firebase:firebase-auth")

    // Thư viện Đăng nhập Google (Bung popup)
    implementation("com.google.android.gms:play-services-auth:21.6.0")

    // Thư viện Cơ sở dữ liệu mây (Realtime Database)
    implementation("com.google.firebase:firebase-database")

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")


    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
}